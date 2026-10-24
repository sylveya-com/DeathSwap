package dev.lokspel.deathswap.world;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.ReflectionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Facade over a pool of reusable game worlds. Each world can host one
 * concurrent match; worlds are created at startup and their chunks reset when a
 * match ends.
 *
 * <p>When {@code worlds.generate-on-start} is enabled the pool is skipped
 * entirely: every match gets a brand-new world generated when the match starts,
 * so the number of simultaneous matches is not capped by {@code worlds.count}.
 */
public class WorldPool {

    private static final GameRule<Integer> RESPAWN_RADIUS_RULE =
            ReflectionUtil.gameRuleOrNull("RESPAWN_RADIUS");

    private final DeathSwap plugin;
    private final WorldReset reset;
    private final WorldLoader loader;
    private final List<WorldInstance> instances = new ArrayList<>();
    private int generatedCount;

    /**
     * Whether a world is currently being generated, either by the startup
     * warm-up or by {@code generate-on-start}.
     */
    @Getter
    private boolean generating;

    public WorldPool(DeathSwap plugin) {
        this.plugin = plugin;
        this.reset = new WorldReset(plugin);
        this.loader = new WorldLoader();

        if (DeathSwapConfig.settings().getWorlds().isGenerateOnStart()) {
            return;
        }

        int count = targetPoolCount();
        for (int i = 0; i < count; i++) {
            instances.add(newInstance(i));
        }

        warmUp();
    }

    /**
     * Acquires a world for a new match.
     *
     * <p>With {@code generate-on-start} enabled a brand-new world is generated
     * here and returned once its spawn area is ready. Since generation cannot
     * finish within this call, {@code null} is returned while it runs and the
     * match start has to be retried.
     *
     * @return the acquired world, or {@code null} if none is available yet
     */
    public World createGameWorld() {
        if (DeathSwapConfig.settings().getWorlds().isGenerateOnStart()) {
            return createFreshWorld();
        }

        WorldInstance instance = findReadyInstance();
        if (instance == null) {
            return null;
        }

        instance.acquire();
        return instance.getWorld();
    }

    /**
     * Releases a world back into the pool.
     *
     * <p>The players are first evacuated to the lobby. The actual world reset
     * is delayed by one tick to ensure that the teleport is completed before
     * the world is unloaded.
     *
     * <p>With {@code generate-on-start} the world is not recycled but deleted
     * outright, since the next match gets a brand-new one.
     */
    public void deleteWorld(World world) {
        WorldInstance instance = findInstance(world);
        if (instance == null) {
            return;
        }

        if (!plugin.isEnabled()) {
            evacuate(instance);
            tearDownNow(instance);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            evacuate(instance);
            Bukkit.getScheduler().runTaskLater(plugin, () -> tearDown(instance), 1L);
        });
    }

    /**
     * Whether the given world belongs to this world pool.
     */
    public boolean isGameWorld(World world) {
        return findInstance(world) != null;
    }

    /**
     * Whether {@code target} belongs to the same match world instance as {@code matchWorld}
     * (the overworld, nether, or end dimension of that match).
     */
    public boolean isMatchWorld(World matchWorld, World target) {
        if (matchWorld == null || target == null) {
            return false;
        }

        WorldInstance instance = findInstance(matchWorld);
        return instance != null && instance.owns(target);
    }

    /**
     * Updates pooled worlds when configuration is reloaded.
     */
    public void reload() {
        if (DeathSwapConfig.settings().getWorlds().isGenerateOnStart()) {
            return;
        }

        int target = targetPoolCount();

        while (instances.size() < target) {
            instances.add(newInstance(instances.size()));
        }

        for (int i = instances.size() - 1; i >= target; i--) {
            WorldInstance instance = instances.get(i);

            if (!instance.isFree()) {
                continue;
            }

            instances.remove(i);
            evacuate(instance);
            reset.discard(instance);
        }

        warmUp();
    }

    /**
     * Clears all pooled worlds synchronously during shutdown.
     *
     * <p>With {@code generate-on-start} the world folders are deleted instead
     * of kept, since no match will ever reuse them.
     */
    public void shutdown() {
        for (WorldInstance instance : instances) {
            evacuate(instance);

            if (DeathSwapConfig.settings().getWorlds().isGenerateOnStart()) {
                reset.discardNow(instance);
                continue;
            }

            reset.resetNow(instance);
        }

        instances.clear();
    }

    /**
     * Generates a brand-new world for a match that is about to start, reusing
     * an already prepared one when several joins queue up at once.
     */
    private World createFreshWorld() {
        WorldInstance ready = findReadyInstance();
        if (ready != null) {
            ready.acquire();
            return ready.getWorld();
        }

        if (!generating) {
            WorldInstance instance = newInstance(generatedCount++);
            instances.add(instance);

            generating = true;
            Bukkit.getScheduler().runTask(plugin, () -> loadInstance(instance, () -> generating = false));
        }

        return null;
    }

    /**
     * Finds a free instance whose worlds are loaded and whose spawn area has
     * finished generating.
     */
    private WorldInstance findReadyInstance() {
        List<WorldInstance> ready = new ArrayList<>();

        for (WorldInstance instance : instances) {
            if (instance.isFree()
                    && instance.isLoaded()
                    && instance.isSpawnReady()) {

                ready.add(instance);
            }
        }

        if (ready.isEmpty()) {
            return null;
        }

        return ready.get(
                ThreadLocalRandom.current().nextInt(ready.size())
        );
    }

    private WorldInstance findInstance(World world) {
        if (world == null) {
            return null;
        }

        for (WorldInstance instance : instances) {
            if (instance.owns(world)) {
                return instance;
            }
        }

        return null;
    }

    private int targetPoolCount() {
        return Math.max(1, DeathSwapConfig.settings().getWorlds().getCount());
    }

    private boolean shouldDiscard() {
        return DeathSwapConfig.settings().getWorlds().isGenerateOnStart()
                || instances.size() > targetPoolCount();
    }

    private WorldInstance newInstance(int index) {
        var worlds = DeathSwapConfig.settings().getWorlds();
        return new WorldInstance(
                worlds.getNamePrefix() + "_" + index,
                loader,
                worlds.isGenerateDimensions()
        );
    }

    /**
     * Loads and pre-generates pooled worlds sequentially.
     *
     * <p>The next world does not start loading until the previous world has
     * completely finished its spawn pre-generation.
     *
     * <pre>
     * ds_0 -> load -> generate -> READY
     *                         |
     *                         v
     * ds_1 -> load -> generate -> READY
     *                         |
     *                         v
     * ds_2 -> load -> generate -> READY
     * </pre>
     */
    private void warmUp() {
        generating = true;
        warmUpNext(0);
    }

    private void warmUpNext(int index) {
        if (index >= instances.size()) {
            generating = false;
            return;
        }

        WorldInstance instance = instances.get(index);

        Bukkit.getScheduler().runTask(plugin, () -> loadInstance(instance, () -> warmUpNext(index + 1)));
    }

    /**
     * Loads a world and starts asynchronous chunk pre-generation.
     *
     * @param instance world instance to load
     * @param onReady  called after the spawn area has finished generating
     */
    private void loadInstance(WorldInstance instance, Runnable onReady) {
        instance.load();

        applySpawnRule(instance);
        applyBorder(instance);

        preGenerateSpawn(instance, onReady);
    }

    /**
     * Applies the configured respawn radius gamerule to every world.
     *
     * <p>Resolves {@code GameRules.RESPAWN_RADIUS} on Paper and
     * {@code GameRule.RESPAWN_RADIUS} on Spigot at runtime; skips the
     * gamerule entirely when neither variant is available.
     */
    private void applySpawnRule(WorldInstance instance) {
        int radius = Math.max(
                0,
                DeathSwapConfig.settings().getWorlds().getSpawnRadius()
        );

        if (RESPAWN_RADIUS_RULE == null) {
            return;
        }

        for (World world : instance.allWorlds()) {
            if (world == null) {
                continue;
            }

            world.setGameRule(
                    RESPAWN_RADIUS_RULE,
                    radius
            );
        }
    }

    /**
     * Applies the configured world border size to every world of the instance.
     */
    private void applyBorder(WorldInstance instance) {
        int size = DeathSwapConfig.settings().getWorlds().getBorder();

        for (World world : instance.allWorlds()) {
            if (world == null) {
                continue;
            }

            WorldBorder border = world.getWorldBorder();

            if (size <= 0) {
                border.reset();
                continue;
            }

            Location spawn = world.getSpawnLocation();

            border.setCenter(
                    spawn.getX(),
                    spawn.getZ()
            );

            border.setSize(size);
        }
    }

    /**
     * Pre-generates the configured spawn area.
     *
     * <p>The callback is invoked only after all requested chunks have finished
     * generating.
     */
    private void preGenerateSpawn(
            WorldInstance instance,
            Runnable onReady
    ) {
        World world = instance.getWorld();

        int radius = DeathSwapConfig.settings().getWorlds().getPreGenerateRadius();

        Location spawn = world.getSpawnLocation();

        int centerX = spawn.getBlockX() >> 4;
        int centerZ = spawn.getBlockZ() >> 4;

        if (radius <= 0) {
            instance.markSpawnReady();
            if (onReady != null) {
                onReady.run();
            }
            return;
        }

        int minX = centerX - radius;
        int minZ = centerZ - radius;
        int maxX = centerX + radius;
        int maxZ = centerZ + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.loadChunk(x, z, true);
            }
        }

        int x = world.getSpawnLocation().getBlockX();
        int z = world.getSpawnLocation().getBlockZ();

        world.setSpawnLocation(
                x,
                world.getHighestBlockYAt(x, z) + 1,
                z
        );

        instance.markSpawnReady();

        if (onReady != null) {
            onReady.run();
        }
    }

    /**
     * Teleports any player still inside the instance's worlds back to the
     * lobby before the worlds are unloaded.
     */
    private void evacuate(WorldInstance instance) {
        Location lobby = DeathSwapConfig.backed().getLobby().toLocation();
        if (lobby == null) {
            return;
        }

        for (World world : instance.allWorlds()) {
            if (world == null) {
                continue;
            }

            for (Player player : world.getPlayers()) {
                player.teleport(lobby);
            }
        }
    }

    /**
     * Tears a released instance down one tick after the evacuation, either
     * recycling it back into the pool or deleting it outright.
     */
    private void tearDown(WorldInstance instance) {
        if (shouldDiscard()) {
            reset.discard(instance);
            instances.remove(instance);
            return;
        }

        reset.reset(instance, () -> loadInstance(instance, null));
    }

    /**
     * Synchronous counterpart of {@link #tearDown}, used when the plugin is
     * already shutting down.
     */
    private void tearDownNow(WorldInstance instance) {
        if (shouldDiscard()) {
            reset.discardNow(instance);
            instances.remove(instance);
            return;
        }

        reset.resetNow(instance);
    }
}
