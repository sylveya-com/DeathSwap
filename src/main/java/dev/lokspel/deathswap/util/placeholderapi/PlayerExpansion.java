package dev.lokspel.deathswap.util.placeholderapi;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.api.DeathSwapAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerExpansion extends PlaceholderExpansion {

    private final DeathSwap plugin;
    private final String identifier;

    public PlayerExpansion(DeathSwap plugin, String identifier) {
        this.plugin = plugin;
        this.identifier = identifier;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        DeathSwapAPI api = DeathSwapAPI.getInstance();
        var cfg = plugin.getMainConfig();

        return switch (params) {
            case "state" -> stateOf(player, api);
            case "deaths" -> String.valueOf(api.getDeaths(player));
            case "deaths_left" -> String.valueOf(Math.max(0, cfg.game().maxDeaths() - api.getDeaths(player)));
            case "max_deaths" -> String.valueOf(cfg.game().maxDeaths());
            case "players_in_lobby" -> String.valueOf(plugin.getGameManager().lobbySize());
            case "min_players" -> String.valueOf(cfg.game().minPlayersToStart());
            case "swap_interval" -> String.valueOf(cfg.game().swapInterval());
            case "next_swap" -> String.valueOf(api.getSecondsUntilNextSwap(player));
            default -> null;
        };
    }

    private String stateOf(Player player, DeathSwapAPI api) {
        if (api.isInLobby(player)) {
            return "lobby";
        }
        if (api.isInMatch(player)) {
            return api.isSpectator(player) ? "spectator" : "match";
        }
        return "none";
    }
}