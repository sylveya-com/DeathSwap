package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.game.MatchManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerDeathListener implements Listener {

    private final DeathSwap plugin;
    private final GameManager game;

    public PlayerDeathListener(DeathSwap plugin) {
        this.plugin = plugin;
        this.game = plugin.getGameManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handle(PlayerDeathEvent event) {
        Player player = event.getEntity();
        game.onPlayerDeath(player);

        if (!plugin.getMainConfig().hide().isolateDeaths()) {
            return;
        }

        MatchManager match = game.findMatchByPlayer(player.getUniqueId());
        if (match == null) {
            return;
        }

        String deathMessage = event.getDeathMessage();
        if (deathMessage == null) {
            return;
        }

        event.setDeathMessage(null);

        List<Player> recipients = new ArrayList<>(match.getOnlinePlayers());
        recipients.forEach(p -> p.sendMessage(deathMessage));
    }
}
