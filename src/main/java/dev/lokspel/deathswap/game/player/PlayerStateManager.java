package dev.lokspel.deathswap.game.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerStateManager {

    private final Map<UUID, PlayerState> states = new HashMap<>();

    public void save(Player player) {
        states.put(player.getUniqueId(), PlayerState.capture(player));
    }

    public void restore(Player player) {
        var state = states.remove(player.getUniqueId());

        if (state != null) {
            state.restore(player);
        }
    }

    public void clear() {
        states.clear();
    }
}
