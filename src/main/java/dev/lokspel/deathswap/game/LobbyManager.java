package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.MessageUtil;
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
        MessageUtil.send(player, DeathSwapConfig.messages().getGame().getJoined());

        if (players.size() < DeathSwapConfig.settings().getGame().getMinPlayersToStart()) {
            showNeedsPlayers();
        }

        if (startTask != null && players.size() >= DeathSwapConfig.settings().getGame().getMinPlayersFastStart()) {
            int target = DeathSwapConfig.settings().getGame().getFastStartDelay();
            if (remainingCountdown > target) {
                remainingCountdown = target;
            }
        }
    }

    public void leave(UUID uuid) {
        if (!players.remove(uuid)) return;
        restoreGameMode(uuid);
        cancelTask();
        if (players.size() < DeathSwapConfig.settings().getGame().getMinPlayersToStart()) {
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
        if (startTask != null || players.size() < DeathSwapConfig.settings().getGame().getMinPlayersToStart()) return;

        remainingCountdown = DeathSwapConfig.settings().getGame().getStartDelay();
        if (players.size() >= DeathSwapConfig.settings().getGame().getMinPlayersFastStart()) {
            remainingCountdown = Math.min(remainingCountdown, DeathSwapConfig.settings().getGame().getFastStartDelay());
        }

        startTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingCountdown <= 0) {
                cancelTask();
                onStart.run();
                return;
            }

            if (players.size() < DeathSwapConfig.settings().getGame().getMinPlayersToStart()) {
                cancelTask();
                return;
            }

            if (DeathSwapConfig.settings().getDisplay().isActionbar()) {
                var messages = DeathSwapConfig.messages();
                var msg = MessageUtil.format(messages.getGame().getStarting(), "seconds", remainingCountdown - 1);
                for (Player player : PlayerUtil.getOnlinePlayers(players)) {
                    PlayerUtil.showActionBar(player, msg);
                }
            }

            remainingCountdown--;
        }, 0L, 20L);
    }

    private void showNeedsPlayers() {
        if (!DeathSwapConfig.settings().getDisplay().isActionbar()) return;
        int required = DeathSwapConfig.settings().getGame().getMinPlayersToStart();
        var messages = DeathSwapConfig.messages();
        var msg = MessageUtil.format(
                messages.getGame().getNeedsPlayers(),
                "current", String.valueOf(players.size()),
                "needed", String.valueOf(required));
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
