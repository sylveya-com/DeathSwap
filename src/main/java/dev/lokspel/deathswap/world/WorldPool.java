package dev.lokspel.deathswap.world;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.util.ReflectionUtil;
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
 */
public class WorldPool {

    private static final GameRule<Integer> RESPAWN_RADIUS_RULE =
            ReflectionUtil.gameRuleOrNull("RESPAWN_RADIUS");

    private final DeathSwap plugin;
    private final WorldReset reset;
    private final List<WorldInstance> instances = new ArrayList<>();

    public WorldPool(DeathSwap plugin) {
        this.plugin = plugin;
        this.reset = new WorldReset(plugin);

        int count = Math.max(1, plugin.getMainConfig().worlds().count());
        String prefix = plugin.getMainConfig().worlds().namePrefix();
        boolean dimensions = plugin.getMainConfig().worlds().generateDimensions();

        WorldLoader loader = new WorldLoader();

        for (int i = 0; i < count; i++) {
            instances.add(new WorldInstance(
                    prefix + "_" + i,
                    loader,
                    dimensions
            ));
        }

        warmUp();
    }

    /**
     * Acquires a free reusable world.
     *
     * @return the acquired world, or {@code null} if all worlds are busy
     */
    public World createGameWorld() {
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
     */
    public void deleteWorld(World world) {
        if (world == null) {
            return;
        }

        for (WorldInstance instance : instances) {
            if (!instance.owns(world)) {
                continue;
            }

            if (!plugin.isEnabled()) {
                evacuate(instance);
                reset.clearNow(instance);
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                evacuate(instance);

                Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> reset.reset(
                                instance,
                                () -> loadInstance(instance)
                        ),
                        1L
                );
            });

            return;
        }
    }

    /**
     * Teleports any player still inside the instance's worlds back to the
     * lobby before the worlds are unloaded.
     */
    private void evacuate(WorldInstance instance) {
        Location lobby = lobbyLocation();

        for (World world : instance.allWorlds()) {
            if (world == null) {
                continue;
            }

            for (Player player : world.getPlayers()) {
                player.teleport(lobby);
            }
        }
    }

    public void teleportToLobby(Player player) {
        player.teleport(lobbyLocation());
    }

    public Location lobbyLocation() {
        return plugin.getMainConfig().lobby().get();
    }

    /**
     * Whether the given world belongs to this world pool.
     */
    public boolean isGameWorld(World world) {
        for (WorldInstance instance : instances) {
            if (instance.owns(world)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Clears all pooled worlds synchronously during shutdown.
     */
    public void shutdown() {
        for (WorldInstance instance : instances) {
            reset.clearNow(instance);
        }
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
     * Loads a world and starts its pre-generation.
     */
    private void loadInstance(WorldInstance instance) {
        loadInstance(instance, () -> {
        });
    }

    /**
     * Applies the configured world border size to every world of the instance.
     */
    private void applyBorder(WorldInstance instance) {
        int size = plugin.getMainConfig().worlds().border();

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
     * Applies the configured respawn radius gamerule to every world.
     *
     * <p>Resolves {@code GameRules.RESPAWN_RADIUS} on Paper and
     * {@code GameRule.RESPAWN_RADIUS} on Spigot at runtime; skips the
     * gamerule entirely when neither variant is available.
     */
    private void applySpawnRule(WorldInstance instance) {
        int radius = Math.max(
                0,
                plugin.getMainConfig().worlds().spawnRadius()
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
        warmUpNext(0);
    }

    private void warmUpNext(int index) {
        if (index >= instances.size()) {
            return;
        }

        WorldInstance instance = instances.get(index);

        Bukkit.getScheduler().runTask(plugin, () -> loadInstance(instance, () -> warmUpNext(index + 1)));
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

        int radius = plugin.getMainConfig()
                .worlds()
                .preGenerateRadius();

        Location spawn = world.getSpawnLocation();

        int centerX = spawn.getBlockX() >> 4;
        int centerZ = spawn.getBlockZ() >> 4;

        if (radius <= 0) {
            instance.markSpawnReady();
            onReady.run();
            return;
        }

        int minX = centerX - radius;
        int minZ = centerZ - radius;
        int maxX = centerX + radius;
        int maxZ = centerZ + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.loadChunk(x, z, false);
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

        onReady.run();
    }
}