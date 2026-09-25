package dev.lokspel.deathswap.world;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
