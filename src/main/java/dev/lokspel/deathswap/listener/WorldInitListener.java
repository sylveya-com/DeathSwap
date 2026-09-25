package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

/**
 * Disables autosave for game worlds; their chunks are reset when a match ends,
 * so saving them to disk is pointless.
 */
public class WorldInitListener implements Listener {

    private final DeathSwap plugin;

    public WorldInitListener(DeathSwap plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handle(WorldInitEvent event) {
        World world = event.getWorld();
        if (world.getName().startsWith(plugin.getMainConfig().worlds().namePrefix() + "_")) {
            world.setAutoSave(false);
        }
    }
}
