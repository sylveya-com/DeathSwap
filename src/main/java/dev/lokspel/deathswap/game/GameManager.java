package dev.lokspel.deathswap.game;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameManager {

    private final DeathSwap plugin;
    private final LobbyManager lobby;
    private final Map<UUID, MatchManager> matches = new HashMap<>();
    private boolean busyNotified;

    public GameManager(DeathSwap plugin) {
        this.plugin = plugin;
        this.lobby = new LobbyManager(plugin);
    }

    public void join(Player player) {
        UUID uuid = player.getUniqueId();

        if (findMatchByPlayer(uuid) != null) {
            player.sendMessage(plugin.getMainConfig().messages().prefixed("already-joined"));
            return;
        }
        if (lobby.contains(uuid)) {
            player.sendMessage(plugin.getMainConfig().messages().prefixed("already-queue"));
            return;
        }
        if (plugin.getMainConfig().lobby().isNotSet()) {
            player.sendMessage(plugin.getMainConfig().messages().prefixed("lobby-not-set"));
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

        player.sendMessage(plugin.getMainConfig().messages().prefixed("left"));
    }

    public void onPlayerDeath(Player player) {
        MatchManager match = findMatchByPlayer(player.getUniqueId());
        if (match != null) {
            match.onPlayerDeath(player);
        }
    }

    public boolean forceStart() {
        if (lobby.size() < 2) return false;
        if (plugin.getMainConfig().lobby().isNotSet()) return false;

        lobby.cancelTask();
        createMatch();
        return true;
    }

    private void createMatch() {
        Set<Player> players = lobby.getOnlinePlayers();
        if (players.size() < 2) return;

        // Acquire a fully spawn-ready world up front (free + loaded + spawn
        // area generated). If none is ready yet, keep players in the lobby,
        // notify once and retry silently until a world becomes available.
        World world = plugin.getWorldPool().createGameWorld();
        if (world == null) {
            if (!busyNotified) {
                var msg = plugin.getMainConfig().messages().prefixed("worlds-busy");
                for (Player player : players) {
                    player.sendMessage(msg);
                }
                busyNotified = true;
            }
            Bukkit.getScheduler().runTaskLater(plugin, this::createMatch, 20L);
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
        for (MatchManager m : List.copyOf(matches.values())) {
            m.stop();
        }
        matches.clear();
        lobby.clear();
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
        return !matches.isEmpty() || lobby.size() > 0;
    }
}
