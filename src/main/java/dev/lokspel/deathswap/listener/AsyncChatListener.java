package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.game.MatchManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChatListener implements Listener {

    private final GameManager game;

    public AsyncChatListener(DeathSwap plugin) {
        this.game = plugin.getGameManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handle(AsyncPlayerChatEvent event) {
        if (!DeathSwapConfig.settings().getHide().isIsolateChat()) {
            return;
        }

        Player sender = event.getPlayer();
        MatchManager match = game.findMatchByPlayer(sender.getUniqueId());

        if (match != null) {
            event.getRecipients().removeIf(recipient -> !match.contains(recipient.getUniqueId()));
            return;
        }

        event.getRecipients().removeIf(recipient ->
                game.findMatchByPlayer(recipient.getUniqueId()) != null
        );
    }
}
