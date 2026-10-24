package dev.lokspel.deathswap.world;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

final class WorldReset {

    private static final Set<String> WORLD_DATA_FOLDERS =
            Set.of("region", "entities", "poi");

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
        World[] worlds = unloadAll(instance);
        if (worlds == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            for (World world : worlds) {
                if (world != null) {
                    clearWorldData(world.getWorldFolder().toPath());
                }
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
        if (worlds == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            for (World world : worlds) {
                if (world != null) {
                    deleteFolder(world.getWorldFolder().toPath());
                }
            }
        });
    }

    /**
     * Synchronous counterpart of {@link #discard}, used when the plugin is
     * already shutting down and the async deletion would never run.
     */
    void discardNow(WorldInstance instance) {
        World[] worlds = unloadAll(instance);
        if (worlds == null) {
            return;
        }

        for (World world : worlds) {
            if (world != null) {
                deleteFolder(world.getWorldFolder().toPath());
            }
        }
    }

    /**
     * Synchronously unloads and clears an instance's worlds. Used on shutdown,
     * where an asynchronous reset would not finish before the server exits.
     */
    void resetNow(WorldInstance instance) {
        World[] worlds = unloadAll(instance);
        if (worlds == null) {
            return;
        }

        for (World world : worlds) {
            if (world != null) {
                clearWorldData(world.getWorldFolder().toPath());
            }
        }
    }

    /**
     * Unloads every world of an instance and returns the handles it held, which
     * stay usable for folder access after the instance forgets them.
     */
    private World[] unloadAll(WorldInstance instance) {
        World[] worlds = instance.allWorlds();
        boolean success = true;

        for (World world : worlds) {
            if (world != null && !Bukkit.unloadWorld(world, false)) {
                plugin.getLogger().warning("Failed to unload game world: " + world.getName());
                success = false;
            }
        }

        if (!success) {
            return null;
        }

        instance.markResetting();
        return worlds;
    }

    private void clearWorldData(Path worldFolder) {
        if (!Files.isDirectory(worldFolder)) return;

        try (var paths = Files.walk(worldFolder)) {
            paths.filter(Files::isDirectory)
                    .filter(path -> WORLD_DATA_FOLDERS.contains(
                            path.getFileName().toString()
                    ))
                    .toList()
                    .forEach(this::deleteFolder);
        } catch (IOException ignored) {
        }
    }

    private void deleteFolder(Path folder) {
        if (!Files.isDirectory(folder)) return;

        try (var paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }
}
