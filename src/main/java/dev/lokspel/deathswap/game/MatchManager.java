package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.api.event.MatchStartEvent;
import dev.lokspel.deathswap.api.event.MatchEndEvent;
import dev.lokspel.deathswap.config.MainConfig;
import dev.lokspel.deathswap.config.MessagesConfig;
import dev.lokspel.deathswap.scoreboard.MatchScoreboard;
import dev.lokspel.deathswap.game.player.PlayerState;
import dev.lokspel.deathswap.game.player.PlayerStateManager;
import dev.lokspel.deathswap.util.PlayerUtil;
import dev.lokspel.deathswap.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MatchManager {

    private final DeathSwap plugin;
    private final MainConfig cfg;
    private final MessagesConfig messages;
    private final World gameWorld;
    private final Set<UUID> playerUuids;
    private final Set<UUID> spectators;
    private final PlayerStateManager states;
    private final DeathManager deaths;
    private final SwapManager swap;
    private final MatchScoreboard scoreboard;
    private final Runnable onEnd;
    private BukkitTask timeoutTask;
    private boolean cleanedUp;

    public MatchManager(DeathSwap plugin, List<Player> players, World gameWorld, Runnable onEnd) {
        this.plugin = plugin;
        this.cfg = plugin.getMainConfig();
        this.messages = cfg.messages();
        this.onEnd = onEnd;
        this.playerUuids = new HashSet<>();
        this.spectators = new HashSet<>();
        this.states = new PlayerStateManager();
        this.deaths = new DeathManager();
        this.swap = new SwapManager(plugin);
        this.scoreboard = new MatchScoreboard(plugin);
        this.cleanedUp = false;

        for (Player player : players) {
            playerUuids.add(player.getUniqueId());
            player.sendMessage(messages.prefixed("world-preparing"));
        }

        this.gameWorld = gameWorld;

        for (Player player : players) {
            player.teleport(gameWorld.getSpawnLocation());
            states.save(player);
            PlayerState.resetForMatch(player);
        }

        deaths.init(playerUuids);
        if (cfg.display().scoreboard()) {
            scoreboard.init(messages.get("scoreboard-title"));
            refreshScoreboard();
        }

        scheduleNextSwap();
        broadcast(messages.prefixed("game-started"));

        int maxTime = cfg.game().maxMatchTime();
        if (maxTime > 0) {
            timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, this::onTimeUp, maxTime * 1200L);
        }

        Bukkit.getPluginManager().callEvent(new MatchStartEvent(players, gameWorld));
    }

    /**
     * Handles a respawn of an in-match player. Returns a random point near the
     * game world's spawn within the configured {@code spawn-radius} (exact spawn
     * when the radius is 0). The location is set explicitly because relying on
     * the default respawn would send eliminated players back to the lobby.
     */
    public Location onPlayerRespawn(Player player) {
        if (!playerUuids.contains(player.getUniqueId())) return null;

        if (spectators.contains(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        Location location = randomSpawn();
        refreshScoreboard();

        if (spectators.contains(player.getUniqueId())) {
            checkWinner();
        }
        return location;
    }

    /**
     * A spawn point near the game world's spawn, randomly offset within the
     * configured {@code spawn-radius}. The Y is placed on the terrain surface to
     * avoid spawning in the air or underground.
     */
    private Location randomSpawn() {
        Location spawn = gameWorld.getSpawnLocation();
        int r = Math.max(0, cfg.worlds().spawnRadius());
        if (r <= 0) return spawn.clone();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = spawn.getBlockX() + random.nextInt(-r, r + 1);
        int z = spawn.getBlockZ() + random.nextInt(-r, r + 1);
        int y = gameWorld.getHighestBlockYAt(x, z) + 1;
        return new Location(gameWorld, x + 0.5, y, z + 0.5, spawn.getYaw(), spawn.getPitch());
    }

    public void onPlayerDeath(Player player) {
        if (!playerUuids.contains(player.getUniqueId())) return;

        int deathCount = deaths.add(player.getUniqueId());
        int max = cfg.game().maxDeaths();

        if (deathCount >= max) {
            spectators.add(player.getUniqueId());
            player.sendMessage(messages.prefixed("eliminated"));
            broadcast(messages.prefixed("player-eliminated", "player", player.getName()));
            checkWinner();
        } else {
            player.sendMessage(messages.prefixed("deaths-left", "deaths", String.valueOf(max - deathCount)));
        }

        refreshScoreboard();
    }

    public void leave(Player player, boolean teleport) {
        if (!playerUuids.contains(player.getUniqueId())) return;

        playerUuids.remove(player.getUniqueId());
        spectators.remove(player.getUniqueId());
        deaths.remove(player.getUniqueId());

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            player.setScoreboard(manager.getMainScoreboard());
        }

        if (teleport) {
            plugin.getWorldPool().teleportToLobby(player);
        }

        refreshScoreboard();
        checkWinner();
    }

    public boolean contains(UUID uuid) {
        return playerUuids.contains(uuid);
    }

    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    public int getDeaths(UUID uuid) {
        return deaths.get(uuid);
    }

    public int getSecondsUntilSwap() {
        return swap.secondsUntilSwap();
    }

    public Set<Player> getOnlinePlayers() {
        return PlayerUtil.getOnlinePlayers(playerUuids);
    }

    public void broadcast(String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void broadcastSound(String soundKey) {
        var sound = SoundUtil.minecraft(soundKey);
        for (Player player : getOnlinePlayers()) {
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    private void refreshScoreboard() {
        if (!cfg.display().scoreboard()) return;
        scoreboard.update(deaths.getAll());
        scoreboard.apply(deaths.getAll().keySet());
    }

    private void scheduleNextSwap() {
        if (deaths.size() < 2) return;
        swap.scheduleNext(this::onSwapComplete, deaths::getAlivePlayers);
    }

    private void onSwapComplete() {
        Set<Player> alive = deaths.getAlivePlayers();

        if (alive.size() < 2) {
            checkWinner();
            return;
        }

        swap.executeSwap(alive);
        scheduleNextSwap();
    }

    private void checkWinner() {
        Set<Player> alive = deaths.getAlivePlayers();
        if (alive.size() > 1) return;

        cancelTasks();

        Player winner = null;
        if (alive.size() == 1) {
            winner = alive.iterator().next();
            broadcast(messages.prefixed("winner", "player", winner.getName()));
            broadcastSound(cfg.sounds().win());
            winner.setGameMode(GameMode.SURVIVAL);
        }

        cleanupNow(winner);
    }

    /**
     * Ends the match when the configured time limit runs out. The survivor (or
     * the alive player with the fewest deaths) wins; if no single leader can be
     * decided the match ends with no winner.
     */
    private void onTimeUp() {
        cancelTasks();

        List<Player> alive = new ArrayList<>(deaths.getAlivePlayers());
        Set<Player> leaders = lowestDeathPlayers(alive);

        Player winner = leaders.size() == 1 ? leaders.iterator().next() : null;
        if (winner != null) {
            broadcast(messages.prefixed("winner", "player", winner.getName()));
            broadcastSound(cfg.sounds().win());
            winner.setGameMode(GameMode.SURVIVAL);
        } else {
            broadcast(messages.prefixed("time-up"));
        }

        cleanupNow(winner);
    }

    private Set<Player> lowestDeathPlayers(List<Player> players) {
        Set<Player> lowest = new LinkedHashSet<>();
        int min = Integer.MAX_VALUE;
        for (Player player : players) {
            int d = deaths.get(player.getUniqueId());
            if (d < min) {
                min = d;
                lowest.clear();
                lowest.add(player);
            } else if (d == min) {
                lowest.add(player);
            }
        }
        return lowest;
    }

    public void stop() {
        cancelTasks();
        cleanupNow(null);
    }

    private void cleanupNow(Player winner) {
        if (cleanedUp) return;
        cleanedUp = true;

        scoreboard.remove(playerUuids);
        for (Player player : getOnlinePlayers()) {
            states.restore(player);
            plugin.getWorldPool().teleportToLobby(player);
        }

        plugin.getWorldPool().deleteWorld(gameWorld);
        deaths.clear();
        states.clear();
        spectators.clear();
        playerUuids.clear();
        onEnd.run();

        Bukkit.getPluginManager().callEvent(new MatchEndEvent(winner));
    }

    private void cancelTasks() {
        swap.cancel();
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
    }
}
