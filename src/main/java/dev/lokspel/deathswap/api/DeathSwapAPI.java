package dev.lokspel.deathswap.api;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.MainConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.game.MatchManager;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@SuppressWarnings("ClassCanBeRecord")
public class DeathSwapAPI {

    @Getter
    private static DeathSwapAPI instance;
    private final DeathSwap plugin;

    public DeathSwapAPI(DeathSwap plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public GameManager getGameManager() {
        return plugin.getGameManager();
    }

    public MainConfig getMainConfig() {
        return plugin.getMainConfig();
    }

    /**
     * Returns the match a player is currently in, or {@code null} if none.
     */
    public MatchManager getMatch(UUID uuid) {
        return plugin.getGameManager().findMatchByPlayer(uuid);
    }

    public MatchManager getMatch(Player player) {
        return getMatch(player.getUniqueId());
    }

    public boolean isInMatch(Player player) {
        return getMatch(player) != null;
    }

    public boolean isSpectator(Player player) {
        MatchManager match = getMatch(player);
        return match != null && match.isSpectator(player.getUniqueId());
    }

    /**
     * Returns the player's current death count, or 0 if not in a match.
     */
    public int getDeaths(Player player) {
        MatchManager match = getMatch(player);
        return match == null ? 0 : match.getDeaths(player.getUniqueId());
    }

    public boolean isInLobby(Player player) {
        return plugin.getGameManager().inLobby(player.getUniqueId());
    }

    /**
     * Seconds remaining until the next position swap for the player's match,
     * or the configured swap interval if the player is not in a match.
     */
    public int getSecondsUntilNextSwap(Player player) {
        MatchManager match = getMatch(player);
        return match == null ? plugin.getMainConfig().game().swapInterval() : match.getSecondsUntilSwap();
    }
}