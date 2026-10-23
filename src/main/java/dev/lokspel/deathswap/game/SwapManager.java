package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.api.event.PlayerSwapEvent;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.MessageUtil;
import dev.lokspel.deathswap.util.PlayerUtil;
import dev.lokspel.deathswap.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
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
        countdownSeconds = Math.max(1, DeathSwapConfig.settings().getGame().getCountdownSeconds());
        totalRemaining = Math.max(countdownSeconds + 1, DeathSwapConfig.settings().getGame().getSwapInterval());
        var messages = DeathSwapConfig.messages();
        var tickSound = SoundUtil.minecraft(DeathSwapConfig.settings().getSounds().getCountdownTick());
        boolean showHud = DeathSwapConfig.settings().getDisplay().isActionbar();

        swapTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            totalRemaining--;

            if (totalRemaining <= 0) {
                swapTask.cancel();
                swapTask = null;
                onSwapComplete.run();
                return;
            }

            if (totalRemaining <= countdownSeconds) {
                var msg = MessageUtil.format(messages.getGame().getCountdown(), "seconds", totalRemaining);
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
            return DeathSwapConfig.settings().getGame().getSwapInterval();
        }
        return Math.max(1, totalRemaining);
    }

    public void executeSwap(Set<Player> alivePlayers) {
        if (alivePlayers.size() < 2) return;

        var messages = DeathSwapConfig.messages();
        var goSound = SoundUtil.minecraft(DeathSwapConfig.settings().getSounds().getCountdownGo());
        var swapSound = SoundUtil.minecraft(DeathSwapConfig.settings().getSounds().getSwap());

        var playerList = new ArrayList<>(alivePlayers);

        var event = new PlayerSwapEvent(playerList);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        var locations = new ArrayList<org.bukkit.Location>(playerList.size());
        for (Player player : playerList) {
            locations.add(player.getLocation());
        }
        Collections.shuffle(locations);

        for (Player player : playerList) {
            if (goSound != null) {
                player.playSound(player.getLocation(), goSound, 1.0f, 1.0f);
            }
        }

        for (int i = 0; i < playerList.size(); i++) {
            playerList.get(i).teleport(locations.get(i));
        }

        for (Player player : playerList) {
            if (swapSound != null) {
                player.playSound(player.getLocation(), swapSound, 1.0f, 1.0f);
            }
            MessageUtil.send(player, messages.getGame().getSwapped());
        }
    }

    public void cancel() {
        if (swapTask != null) {
            swapTask.cancel();
            swapTask = null;
        }
    }
}
