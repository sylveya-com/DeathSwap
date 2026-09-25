package dev.lokspel.deathswap.config.section;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.configuration.file.FileConfiguration;

public class WorldsSection {

    private static final String PATH = "worlds.";

    private final DeathSwap plugin;

    public WorldsSection(DeathSwap plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public int count() {
        return config().getInt(PATH + "count", 5);
    }

    public int preGenerateRadius() {
        return config().getInt(PATH + "pre-generate-radius", 7);
    }

    public int spawnRadius() {
        return config().getInt(PATH + "spawn-radius", 100);
    }

    public int border() {
        return config().getInt(PATH + "border", 3000);
    }

    public String namePrefix() {
        return config().getString(PATH + "name-prefix", "deathswap");
    }

    public boolean generateDimensions() {
        return config().getBoolean(PATH + "generate-dimensions", true);
    }
}
