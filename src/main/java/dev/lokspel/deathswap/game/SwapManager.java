package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.api.event.PlayerSwapEvent;
import dev.lokspel.deathswap.util.PlayerUtil;
import dev.lokspel.deathswap.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;

public class SwapManager {

    private final DeathSwap plugin;
    private BukkitTask swapTask;
    private int totalRemaining;
    private int countdownSeconds;

    public SwapManager(DeathSwap plugin) {
        this.plugin = plugin;
    }

    public void scheduleNext(Runnable onSwapComplete, Supplier<Set<Player>> aliveSupplier) {
        var cfg = plugin.getMainConfig();
        countdownSeconds = Math.max(1, cfg.game().countdownSeconds());
        totalRemaining = Math.max(countdownSeconds + 1, cfg.game().swapInterval());
        var messages = cfg.messages();
        var tickSound = SoundUtil.minecraft(cfg.sounds().countdownTick());
        boolean showHud = cfg.display().actionbar();

        swapTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            totalRemaining--;

            if (totalRemaining <= 0) {
                swapTask.cancel();
                swapTask = null;
                onSwapComplete.run();
                return;
            }

            if (totalRemaining <= countdownSeconds) {
                var msg = messages.get("countdown", "seconds", String.valueOf(totalRemaining));
                var alive = aliveSupplier.get();

                for (Player player : alive) {
                    if (showHud) {
                        PlayerUtil.showCountdownTitle(player, msg);
                    }
                    if (tickSound != null) {
                        player.playSound(player.getLocation(), tickSound, 1.0f, 1.0f);
                    }
                }
            }
        }, 1L, 20L);
    }

    /**
     * Seconds remaining until the next swap as a single continuous countdown,
     * or the full configured interval when no swap is pending. 0 means a swap
     * is happening right now.
     */
    public int secondsUntilSwap() {
        if (swapTask == null) {
            return plugin.getMainConfig().game().swapInterval();
        }
        return Math.max(1, totalRemaining);
    }

    public void executeSwap(Set<Player> alivePlayers) {
        if (alivePlayers.size() < 2) return;

        var cfg = plugin.getMainConfig();
        var messages = cfg.messages();
        var goSound = SoundUtil.minecraft(cfg.sounds().countdownGo());
        var swapSound = SoundUtil.minecraft(cfg.sounds().swap());

        var playerList = new ArrayList<>(alivePlayers);
        var first = playerList.get(0);
        var second = playerList.get(1);

        var event = new PlayerSwapEvent(first, second);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        var loc1 = first.getLocation();
        var loc2 = second.getLocation();

        for (Player player : playerList) {
            if (goSound != null) {
                player.playSound(player.getLocation(), goSound, 1.0f, 1.0f);
            }
        }

        first.teleport(loc2);
        second.teleport(loc1);

        for (Player player : playerList) {
            if (swapSound != null) {
                player.playSound(player.getLocation(), swapSound, 1.0f, 1.0f);
            }
            player.sendMessage(messages.prefixed("swap-message"));
        }
    }

    public void cancel() {
        if (swapTask != null) {
            swapTask.cancel();
            swapTask = null;
        }
    }
}
