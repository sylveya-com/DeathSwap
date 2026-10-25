package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.Disableable;
import dev.lokspel.deathswap.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameManager implements Disableable {

    private final DeathSwap plugin;
    private final LobbyManager lobby;
    private final Map<UUID, MatchManager> matches = new ConcurrentHashMap<>();
    private boolean busyNotified;
    private BukkitTask retryTask;

    public GameManager(DeathSwap plugin) {
        this.plugin = plugin;
        this.lobby = new LobbyManager(plugin);
    }

    public void join(Player player) {
        UUID uuid = player.getUniqueId();

        var messages = DeathSwapConfig.messages();
        if (findMatchByPlayer(uuid) != null) {
            MessageUtil.send(player, messages.getGame().getAlreadyJoined());
            return;
        }
        if (lobby.contains(uuid)) {
            MessageUtil.send(player, messages.getGame().getAlreadyQueue());
            return;
        }
        if (!DeathSwapConfig.backed().getLobby().isSet()) {
            MessageUtil.send(player, messages.getCommands().getLobbyNotSet());
            return;
        }

        lobby.join(player);
        lobby.tryAutoStart(this::createMatch);
    }

    public void leave(Player player) {
        leave(player, true);
    }

    public void leave(Player player, boolean teleport) {
        UUID uuid = player.getUniqueId();
        MatchManager match = findMatchByPlayer(uuid);

        if (match != null) {
            match.leave(player, teleport);
        } else if (lobby.contains(uuid)) {
            lobby.leave(uuid);
        } else {
            return;
        }

        MessageUtil.send(player, DeathSwapConfig.messages().getGame().getLeft());
    }

    public void onPlayerDeath(Player player) {
        MatchManager match = findMatchByPlayer(player.getUniqueId());
        if (match != null) {
            match.onPlayerDeath(player);
        }
    }

    public boolean forceStart() {
        if (lobby.size() < DeathSwapConfig.settings().getGame().getMinPlayersToStart()) return false;
        if (!DeathSwapConfig.backed().getLobby().isSet()) return false;

        lobby.cancelTask();
        createMatch();
        return true;
    }

    private void createMatch() {
        cancelRetryTask();

        Set<Player> players = lobby.getOnlinePlayers();
        if (players.size() < DeathSwapConfig.settings().getGame().getMinPlayersToStart()) {
            busyNotified = false;
            return;
        }

        // Acquire a fully spawn-ready world up front (free + loaded + spawn
        // area generated). If none is ready yet, keep players in the lobby,
        // notify once and retry silently until a world becomes available.
        World world = plugin.getWorldPool().createGameWorld();
        if (world == null) {
            if (!busyNotified) {
                var messages = DeathSwapConfig.messages();
                String msg = plugin.getWorldPool().isGenerating()
                        ? messages.getGame().getWorldPreparing()
                        : messages.getGame().getWorldsBusy();

                MessageUtil.broadcast(players, msg);
                busyNotified = true;
            }
            retryTask = Bukkit.getScheduler().runTaskLater(plugin, this::createMatch, 20L);
            return;
        }
        busyNotified = false;

        lobby.clear();

        UUID matchId = UUID.randomUUID();
        matches.put(matchId, new MatchManager(plugin, new ArrayList<>(players), world, () -> {
            matches.remove(matchId);
            if (plugin.getPlayerHider() != null) {
                plugin.getPlayerHider().refreshVisibility();
            }
        }));
        if (plugin.getPlayerHider() != null) {
            plugin.getPlayerHider().refreshVisibility();
        }
    }

    public void stop() {
        cancelRetryTask();
        busyNotified = false;
        if (lobby.size() > 0) {
            MessageUtil.broadcast(lobby.getOnlinePlayers(), DeathSwapConfig.messages().getGame().getStopped());
        }
        for (MatchManager m : List.copyOf(matches.values())) {
            m.stop();
        }
        matches.clear();
        lobby.clear();
    }

    private void cancelRetryTask() {
        if (retryTask != null) {
            retryTask.cancel();
            retryTask = null;
        }
    }

    public MatchManager findMatchByPlayer(UUID uuid) {
        for (MatchManager m : matches.values()) {
            if (m.contains(uuid)) return m;
        }
        return null;
    }

    public boolean isParticipant(Player player) {
        return findMatchByPlayer(player.getUniqueId()) != null || lobby.contains(player.getUniqueId());
    }

    public boolean inLobby(UUID uuid) {
        return lobby.contains(uuid);
    }

    public int lobbySize() {
        return lobby.size();
    }

    public boolean hasActivity() {
        return !matches.isEmpty() || lobby.size() > 0 || retryTask != null;
    }

    @Override
    public void disable() {
        stop();
    }
}
