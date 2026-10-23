package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.game.MatchManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * Removes active match participants who leave the game world from the match.
 */
public final class PlayerTeleportListener implements Listener {

    private final DeathSwap plugin;
    private final GameManager game;

    public PlayerTeleportListener(DeathSwap plugin) {
        this.plugin = plugin;
        this.game = plugin.getGameManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        MatchManager match = game.findMatchByPlayer(playerId);
        if (match == null || !match.isActive()) {
            return;
        }

        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        // Allow teleports to any world owned by the game world pool
        // (overworld, nether, and end dimensions of the same match).
        if (plugin.getWorldPool().isGameWorld(to.getWorld())) {
            return;
        }

        match.leave(player, false);
    }
}
