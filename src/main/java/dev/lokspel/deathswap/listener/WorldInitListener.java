package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

/**
 * Disables autosave for game worlds; their chunks are reset when a match ends,
 * so saving them to disk is pointless.
 */
public class WorldInitListener implements Listener {

    public WorldInitListener(DeathSwap ignored) {
    }

    @EventHandler
    public void handle(WorldInitEvent event) {
        World world = event.getWorld();
        if (world.getName().startsWith(DeathSwapConfig.settings().getWorlds().getNamePrefix() + "_")) {
            world.setAutoSave(false);
        }
    }
}
