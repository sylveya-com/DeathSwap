package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.api.event.MatchStartEvent;
import dev.lokspel.deathswap.api.event.MatchEndEvent;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.scoreboard.MatchScoreboard;
import dev.lokspel.deathswap.game.player.PlayerState;
import dev.lokspel.deathswap.util.CommandUtil;
import dev.lokspel.deathswap.util.MessageUtil;
import dev.lokspel.deathswap.util.PlayerUtil;
import dev.lokspel.deathswap.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MatchManager {

    private final DeathSwap plugin;
    @Getter
    private final World gameWorld;
    private final Set<UUID> playerUuids;
    private final Set<UUID> spectators;
    private final Map<UUID, PlayerState> states;
    private final DeathManager deaths;
    private final SwapManager swap;
    private final MatchScoreboard scoreboard;
    private final Runnable onEnd;
    private BukkitTask timeoutTask;
    private boolean cleanedUp;

    public MatchManager(DeathSwap plugin, List<Player> players, World gameWorld, Runnable onEnd) {
        this.plugin = plugin;
        this.onEnd = onEnd;
        this.playerUuids = ConcurrentHashMap.newKeySet();
        this.spectators = ConcurrentHashMap.newKeySet();
        this.states = new HashMap<>();
        this.deaths = new DeathManager();
        this.swap = new SwapManager(plugin);
        this.scoreboard = new MatchScoreboard();
        this.cleanedUp = false;

        for (Player player : players) {
            playerUuids.add(player.getUniqueId());
        }

        this.gameWorld = gameWorld;

        for (Player player : players) {
            player.teleport(randomSpawn());
            states.put(player.getUniqueId(), PlayerState.capture(player));
            PlayerState.resetForMatch(player);
        }

        deaths.init(playerUuids);
        var messages = DeathSwapConfig.messages();
        if (DeathSwapConfig.settings().getDisplay().isScoreboard()) {
            scoreboard.init(MessageUtil.format(messages.getScoreboard().getTitle()));
            refreshScoreboard();
        }

        scheduleNextSwap();
        broadcast(MessageUtil.format(messages.getGame().getStarted()));

        int maxTime = DeathSwapConfig.settings().getGame().getMaxMatchTime();
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

        if (cleanedUp) {
            Location lobby = DeathSwapConfig.backed().getLobby().toLocation();
            if (lobby != null) {
                return lobby;
            }
        }

        boolean eliminated = spectators.contains(player.getUniqueId());
        if (eliminated) {
            player.setGameMode(GameMode.SPECTATOR);
            CommandUtil.run(DeathSwapConfig.settings().getCommands().getOnMatchEnd(), player);
        }

        Location location = randomSpawn();
        refreshScoreboard();
        return location;
    }

    /**
     * A spawn point near the game world's spawn, randomly offset within the
     * configured {@code spawn-radius}. The Y is placed on the terrain surface to
     * avoid spawning in the air or underground.
     */
    private Location randomSpawn() {
        Location spawn = gameWorld.getSpawnLocation();
        int r = Math.max(0, DeathSwapConfig.settings().getWorlds().getSpawnRadius());
        if (r == 0) return spawn.clone();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = spawn.getBlockX() + random.nextInt(-r, r + 1);
        int z = spawn.getBlockZ() + random.nextInt(-r, r + 1);
        int y = gameWorld.getHighestBlockYAt(x, z) + 1;
        return new Location(gameWorld, x + 0.5, y, z + 0.5, spawn.getYaw(), spawn.getPitch());
    }

    public void onPlayerDeath(Player player) {
        if (!playerUuids.contains(player.getUniqueId())) return;

        int deathCount = deaths.add(player.getUniqueId());
        int max = DeathSwapConfig.settings().getGame().getMaxDeaths();

        var messages = DeathSwapConfig.messages();
        if (deathCount >= max) {
            spectators.add(player.getUniqueId());
            MessageUtil.send(player, messages.getGame().getEliminated());
            broadcast(MessageUtil.format(messages.getGame().getPlayerEliminated(), "player", player.getName()));
            checkWinner();
        } else {
            MessageUtil.send(player, messages.getGame().getDeathsLeft(), "deaths", max - deathCount);
        }

        refreshScoreboard();
    }

    public void leave(Player player, boolean teleport) {
        if (!playerUuids.contains(player.getUniqueId())) return;

        boolean eliminated = spectators.remove(player.getUniqueId());
        playerUuids.remove(player.getUniqueId());
        deaths.remove(player.getUniqueId());

        PlayerState state = states.remove(player.getUniqueId());
        if (state != null) {
            state.restore(player);
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            player.setScoreboard(manager.getMainScoreboard());
        }

        if (teleport) {
            Location lobby = DeathSwapConfig.backed().getLobby().toLocation();
            if (lobby != null) {
                player.teleport(lobby);
            }
        }

        if (!eliminated) {
            CommandUtil.run(DeathSwapConfig.settings().getCommands().getOnMatchEnd(), player);
        }

        refreshScoreboard();
        checkWinner();
    }

    public boolean contains(UUID uuid) {
        return playerUuids.contains(uuid);
    }

    public boolean isActive() {
        return !cleanedUp;
    }

    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    /**
     * Whether the given world belongs to this match (overworld, nether, or end).
     */
    public boolean isInMatchWorld(World world) {
        return plugin.getWorldPool().isMatchWorld(gameWorld, world);
    }

    public Set<Player> getAlivePlayers() {
        return deaths.getAlivePlayers(spectators);
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
        if (!DeathSwapConfig.settings().getDisplay().isScoreboard()) return;
        scoreboard.update(deaths.getAll());
        scoreboard.apply(deaths.getAll().keySet());
    }

    private void scheduleNextSwap() {
        if (deaths.size() < 2) return;
        swap.scheduleNext(this::onSwapComplete, this::getAlivePlayers);
    }

    private void onSwapComplete() {
        Set<Player> alive = getAlivePlayers();

        if (alive.size() < 2) {
            checkWinner();
            return;
        }

        swap.executeSwap(alive);
        scheduleNextSwap();
    }

    private void checkWinner() {
        Set<Player> alive = getAlivePlayers();
        if (alive.size() > 1) return;

        cancelTasks();

        Player winner = alive.size() == 1 ? alive.iterator().next() : null;
        if (winner != null) {
            var messages = DeathSwapConfig.messages();
            broadcast(MessageUtil.format(messages.getGame().getWinner(), "player", winner.getName()));
            broadcastSound(DeathSwapConfig.settings().getSounds().getWin());
            winner.setGameMode(GameMode.SURVIVAL);
        }

        if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(plugin, () -> cleanupNow(winner));
        } else {
            cleanupNow(winner);
        }
    }

    /**
     * Ends the match when the configured time limit runs out. The survivor (or
     * the alive player with the fewest deaths) wins; if no single leader can be
     * decided the match ends with no winner.
     */
    private void onTimeUp() {
        cancelTasks();

        List<Player> alive = new ArrayList<>(getAlivePlayers());
        Set<Player> leaders = lowestDeathPlayers(alive);

        var messages = DeathSwapConfig.messages();
        Player winner = leaders.size() == 1 ? leaders.iterator().next() : null;
        if (winner != null) {
            broadcast(MessageUtil.format(messages.getGame().getWinner(), "player", winner.getName()));
            broadcastSound(DeathSwapConfig.settings().getSounds().getWin());
            winner.setGameMode(GameMode.SURVIVAL);
        } else {
            broadcast(MessageUtil.format(messages.getGame().getTimeUp()));
        }

        if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(plugin, () -> cleanupNow(winner));
        } else {
            cleanupNow(winner);
        }
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
        Location lobby = DeathSwapConfig.backed().getLobby().toLocation();
        for (Player player : getOnlinePlayers()) {
            if (player.isDead()) {
                player.spigot().respawn();
            }
            PlayerState state = states.remove(player.getUniqueId());
            if (state != null) {
                state.restore(player);
            }
            if (lobby != null) {
                player.teleport(lobby);
            }
            if (!spectators.contains(player.getUniqueId())) {
                CommandUtil.run(DeathSwapConfig.settings().getCommands().getOnMatchEnd(), player);
            }
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
