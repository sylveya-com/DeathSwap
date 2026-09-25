package dev.lokspel.deathswap.config.section;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.configuration.file.FileConfiguration;

public class HideSection {

    private static final String PATH = "hide.";

    private final DeathSwap plugin;

    public HideSection(DeathSwap plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public boolean matchPlayersInTab() {
        return config().getBoolean(PATH + "match-players-in-tab", false);
    }

    public boolean isolateChat() {
        return config().getBoolean(PATH + "isolate-chat", true);
    }

    public boolean isolateDeaths() {
        return config().getBoolean(PATH + "isolate-deaths", true);
    }

    public boolean isolateAchievements() {
        return config().getBoolean(PATH + "isolate-achievements", true);
    }
}