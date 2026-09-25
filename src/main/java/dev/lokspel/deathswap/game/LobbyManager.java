package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LobbyManager {

    private final DeathSwap plugin;
    private final Set<UUID> players = new HashSet<>();
    private final Map<UUID, GameMode> previousGameModes = new HashMap<>();
    private BukkitTask startTask;
    private int remainingCountdown;

    public LobbyManager(DeathSwap plugin) {
        this.plugin = plugin;
    }

    public void join(Player player) {
        if (!players.add(player.getUniqueId())) return;

        previousGameModes.put(player.getUniqueId(), player.getGameMode());
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage(plugin.getMainConfig().messages().prefixed("joined"));

        if (players.size() < plugin.getMainConfig().game().minPlayersToStart()) {
            showNeedsPlayers();
        }

        if (startTask != null && players.size() >= plugin.getMainConfig().game().minPlayersFastStart()) {
            int target = plugin.getMainConfig().game().fastStartDelay();
            if (remainingCountdown > target) {
                remainingCountdown = target;
            }
        }
    }

    public void leave(UUID uuid) {
        if (!players.remove(uuid)) return;
        restoreGameMode(uuid);
        cancelTask();
        if (players.size() < plugin.getMainConfig().game().minPlayersToStart()) {
            showNeedsPlayers();
        }
    }

    public boolean contains(UUID uuid) {
        return players.contains(uuid);
    }

    public Set<Player> getOnlinePlayers() {
        return PlayerUtil.getOnlinePlayers(players);
    }

    public int size() {
        return players.size();
    }

    public void clear() {
        cancelTask();
        players.forEach(this::restoreGameMode);
        previousGameModes.clear();
        players.clear();
    }

    public void cancelTask() {
        if (startTask == null) return;
        startTask.cancel();
        startTask = null;
    }

    public void tryAutoStart(Runnable onStart) {
        var cfg = plugin.getMainConfig();
        if (startTask != null || players.size() < cfg.game().minPlayersToStart()) return;

        remainingCountdown = cfg.game().startDelay();
        if (players.size() >= cfg.game().minPlayersFastStart()) {
            remainingCountdown = Math.min(remainingCountdown, cfg.game().fastStartDelay());
        }

        startTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingCountdown <= 0) {
                cancelTask();
                onStart.run();
                return;
            }

            if (players.size() < cfg.game().minPlayersToStart()) {
                cancelTask();
                return;
            }

            if (cfg.display().actionbar()) {
                var msg = cfg.messages().get("starting", "seconds", String.valueOf(remainingCountdown - 1));
                for (Player player : PlayerUtil.getOnlinePlayers(players)) {
                    PlayerUtil.showActionBar(player, msg);
                }
            }

            remainingCountdown--;
        }, 0L, 20L);
    }

    private void showNeedsPlayers() {
        if (!plugin.getMainConfig().display().actionbar()) return;
        int required = plugin.getMainConfig().game().minPlayersToStart();
        var msg = plugin.getMainConfig().messages().get(
                "needs-players",
                "players", String.valueOf(players.size()),
                "required", String.valueOf(required));
        for (Player player : getOnlinePlayers()) {
            PlayerUtil.showActionBar(player, msg);
        }
    }

    private void restoreGameMode(UUID uuid) {
        Player player = PlayerUtil.getOnlinePlayer(uuid);
        if (player == null) return;
        player.setGameMode(previousGameModes.getOrDefault(uuid, GameMode.SURVIVAL));
        previousGameModes.remove(uuid);
    }
}
