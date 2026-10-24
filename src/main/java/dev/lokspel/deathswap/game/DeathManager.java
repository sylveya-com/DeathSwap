package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Collections;
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
        return getAlivePlayers(Collections.emptySet());
    }

    public Set<Player> getAlivePlayers(Set<UUID> spectators) {
        int maxDeaths = DeathSwapConfig.settings().getGame().getMaxDeaths();
        Set<Player> alive = PlayerUtil.getOnlinePlayers(counts.keySet());
        alive.removeIf(p -> (spectators != null && spectators.contains(p.getUniqueId()))
                || (maxDeaths > 0 && counts.getOrDefault(p.getUniqueId(), 0) >= maxDeaths)
                || p.getGameMode() == GameMode.SPECTATOR);
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
