package dev.lokspel.deathswap.config.section;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.configuration.file.FileConfiguration;

public class SoundsSection {

    private static final String PATH = "sounds.";

    private final DeathSwap plugin;

    public SoundsSection(DeathSwap plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public String countdownTick() {
        return config().getString(PATH + "countdown-tick", "entity.note.pling");
    }

    public String countdownGo() {
        return config().getString(PATH + "countdown-go", "entity.experience_orb.pickup");
    }

    public String swap() {
        return config().getString(PATH + "swap", "entity.enderman.teleport");
    }

    public String win() {
        return config().getString(PATH + "win", "ui.toast.challenge_complete");
    }
}
