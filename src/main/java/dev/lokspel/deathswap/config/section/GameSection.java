package dev.lokspel.deathswap.config.section;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.configuration.file.FileConfiguration;

public class GameSection {

    private static final String PATH = "game.";

    private final DeathSwap plugin;

    public GameSection(DeathSwap plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public int swapInterval() {
        return config().getInt(PATH + "swap-interval", 180);
    }

    public int maxDeaths() {
        return config().getInt(PATH + "max-deaths", 5);
    }

    public int maxMatchTime() {
        return config().getInt(PATH + "max-match-time", 0);
    }

    public int countdownSeconds() {
        return config().getInt(PATH + "countdown-seconds", 5);
    }

    public int startDelay() {
        return config().getInt(PATH + "start-delay", 10);
    }

    public int minPlayersToStart() {
        return config().getInt(PATH + "min-players-to-start", 2);
    }

    public int minPlayersFastStart() {
        return config().getInt(PATH + "min-players-fast-start", 4);
    }

    public int fastStartDelay() {
        return config().getInt(PATH + "fast-start-delay", 3);
    }

    public boolean pvpEnabled() {
        return config().getBoolean(PATH + "pvp-enabled", true);
    }
}
