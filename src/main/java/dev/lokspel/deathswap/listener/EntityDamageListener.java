package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {

    private final GameManager game;

    public EntityDamageListener(DeathSwap plugin) {
        this.game = plugin.getGameManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handle(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) return;
        if (!(event.getDamager() instanceof Player damager)) return;

        if (game.findMatchByPlayer(damaged.getUniqueId()) == null) return;
        if (game.findMatchByPlayer(damager.getUniqueId()) == null) return;

        if (!DeathSwapConfig.settings().getGame().isPvpEnabled()) {
            event.setCancelled(true);
        }
    }
}
