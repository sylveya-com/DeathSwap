package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeathManager {

    private final Map<UUID, Integer> counts = new HashMap<>();

    public void init(Set<UUID> players) {
        counts.clear();
        for (UUID uuid : players) {
            counts.put(uuid, 0);
        }
    }

    public int add(UUID uuid) {
        return counts.merge(uuid, 1, Integer::sum);
    }

    public void remove(UUID uuid) {
        counts.remove(uuid);
    }

    public Set<Player> getAlivePlayers() {
        Set<Player> alive = PlayerUtil.getOnlinePlayers(counts.keySet());
        alive.removeIf(p -> p.getGameMode() == GameMode.SPECTATOR);
        return alive;
    }

    public int get(UUID uuid) {
        return counts.getOrDefault(uuid, 0);
    }

    public Map<UUID, Integer> getAll() {
        return new HashMap<>(counts);
    }

    public int size() {
        return counts.size();
    }

    public void clear() {
        counts.clear();
    }
}
