package dev.lokspel.deathswap.game.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStateManager {

    private final Map<UUID, PlayerState> states = new ConcurrentHashMap<>();

    public void capture(Player player) {
        states.put(player.getUniqueId(), PlayerState.capture(player));
        PlayerState.resetForMatch(player);
    }

    public void restore(Player player) {
        PlayerState state = states.remove(player.getUniqueId());
        if (state != null) {
            state.restore(player);
        }
    }

    public void restoreAll() {
        for (UUID uuid : states.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                restore(player);
            }
        }
        states.clear();
    }
}
