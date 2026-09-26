package dev.lokspel.deathswap.world;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

/**
 * Resets a released world instance: unloads its world, clears the flat set of
 * {@code .mca} chunk files off the main thread, then marks the instance free
 * and lets it be reloaded on the server thread.
 */
final class WorldReset {

    private final DeathSwap plugin;

    WorldReset(DeathSwap plugin) {
        this.plugin = plugin;
    }

    /**
     * Resets an instance's world so it is ready for the next match.
     *
     * @param instance   the instance holding the world to reset
     * @param onReloaded run on the main thread after the world is reloaded
     */
    void reset(WorldInstance instance, Runnable onReloaded) {
        World[] worlds = instance.allWorlds();
        for (World world : worlds) {
            Bukkit.unloadWorld(world, false);
        }
        instance.markResetting();

        CompletableFuture.runAsync(() -> {
            for (World world : worlds) {
                clearRegionFiles(world.getWorldFolder().toPath().resolve("region"));
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                instance.reset();
                onReloaded.run();
            });
        });
    }

    /**
     * Unloads and deletes an instance's worlds without reloading them, so the
     * instance is not reused. Used when every match gets a brand-new world and
     * the old one has to disappear from disk entirely.
     *
     * <p>The instance stays tracked so a player still respawning inside the
     * deleted world is recognized as being in a game world. It holds no world
     * handles anymore, so it is never handed out again.
     */
    void discard(WorldInstance instance) {
        World[] worlds = unloadAll(instance);

        CompletableFuture.runAsync(() -> {
            for (World world : worlds) {
                if (world == null) continue;

                deleteFolder(world.getWorldFolder().toPath());
            }
        });
    }

    /**
     * Synchronous counterpart of {@link #discard}, used when the plugin is
     * already shutting down and the async deletion would never run.
     */
    void discardNow(WorldInstance instance) {
        for (World world : unloadAll(instance)) {
            if (world == null) continue;

            deleteFolder(world.getWorldFolder().toPath());
        }
    }

    /**
     * Unloads every world of an instance and returns the handles it held, which
     * stay usable for folder access after the instance forgets them.
     */
    private World[] unloadAll(WorldInstance instance) {
        World[] worlds = instance.allWorlds();

        for (World world : worlds) {
            if (world != null) {
                Bukkit.unloadWorld(world, false);
            }
        }

        instance.markResetting();
        return worlds;
    }

    /**
     * Synchronously unloads and clears an instance's worlds. Used on shutdown,
     * where an asynchronous reset would not finish before the server exits.
     */
    void clearNow(WorldInstance instance) {
        for (World world : instance.allWorlds()) {
            if (world == null) continue;

            Bukkit.unloadWorld(world, false);
            clearRegionFiles(world.getWorldFolder().toPath().resolve("region"));
        }
    }

    private void clearRegionFiles(Path regionFolder) {
        if (!Files.isDirectory(regionFolder)) return;

        try (var files = Files.list(regionFolder)) {
            files.forEach(child -> {
                try {
                    Files.deleteIfExists(child);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private void deleteFolder(Path folder) {
        if (!Files.isDirectory(folder)) return;

        try (var paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder()).forEach(child -> {
                try {
                    Files.deleteIfExists(child);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
