package dev.lokspel.deathswap.config.section;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.configuration.file.FileConfiguration;

public class DisplaySection {

    private static final String PATH = "display.";

    private final DeathSwap plugin;

    public DisplaySection(DeathSwap plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public boolean scoreboard() {
        return config().getBoolean(PATH + "scoreboard", true);
    }

    public boolean actionbar() {
        return config().getBoolean(PATH + "actionbar", true);
    }
}