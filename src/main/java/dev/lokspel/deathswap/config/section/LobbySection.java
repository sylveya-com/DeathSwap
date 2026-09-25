package dev.lokspel.deathswap.config.section;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class LobbySection {

    private static final String PATH = "lobby.";

    private final DeathSwap plugin;

    public LobbySection(DeathSwap plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public void set(Location location) {
        var cfg = config();
        var world = location.getWorld();
        if (world == null) return;
        cfg.set(PATH + "world", world.getName());
        cfg.set(PATH + "x", location.getX());
        cfg.set(PATH + "y", location.getY());
        cfg.set(PATH + "z", location.getZ());
        cfg.set(PATH + "yaw", (double) location.getYaw());
        cfg.set(PATH + "pitch", (double) location.getPitch());
        plugin.saveConfig();
    }

    public boolean isNotSet() {
        return !config().contains(PATH + "world");
    }

    public Location get() {
        var cfg = config();
        if (!cfg.contains(PATH + "world")) return null;

        String worldName = cfg.getString(PATH + "world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(world,
            cfg.getDouble(PATH + "x"),
            cfg.getDouble(PATH + "y"),
            cfg.getDouble(PATH + "z"),
            (float) cfg.getDouble(PATH + "yaw"),
            (float) cfg.getDouble(PATH + "pitch"));
    }
}
