package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.game.MatchManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final DeathSwap plugin;
    private final GameManager game;

    public PlayerRespawnListener(DeathSwap plugin) {
        this.plugin = plugin;
        this.game = plugin.getGameManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handle(PlayerRespawnEvent event) {
        MatchManager match = game.findMatchByPlayer(event.getPlayer().getUniqueId());
        if (match != null) {
            Location location = match.onPlayerRespawn(event.getPlayer());
            if (location != null) {
                event.setRespawnLocation(location);
            }
            return;
        }

        // The player died in a game world but the match has already ended and
        // its world is being torn down (e.g. eliminated in the final seconds).
        // Respawn them in the lobby instead of into the deleted world's void.
        if (plugin.getWorldPool().isGameWorld(event.getPlayer().getWorld())) {
            event.setRespawnLocation(plugin.getWorldPool().lobbyLocation());
        }
    }
}
