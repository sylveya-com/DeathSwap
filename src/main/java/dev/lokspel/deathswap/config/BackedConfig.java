package dev.lokspel.deathswap.config;

import dev.lokspel.deathswap.DeathSwap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class BackedConfig {

    private static final String FILE_NAME = "backed.yml";

    private final DeathSwap plugin;
    private final File file;
    private YamlConfiguration config;

    public BackedConfig(DeathSwap plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isSet(String name) {
        return !config.getString(name + ".world", "").isEmpty();
    }

    public Location location(String name) {
        String path = name + ".";
        World world = Bukkit.getWorld(config.getString(path + "world", ""));
        if (world == null) {
            return null;
        }
        return new Location(
                world,
                config.getDouble(path + "x"),
                config.getDouble(path + "y"),
                config.getDouble(path + "z"),
                (float) config.getDouble(path + "yaw"),
                (float) config.getDouble(path + "pitch")
        );
    }

    public void setLocation(String name, Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        String path = name + ".";
        config.set(path + "world", world.getName());
        config.set(path + "x", location.getX());
        config.set(path + "y", location.getY());
        config.set(path + "z", location.getZ());
        config.set(path + "yaw", (double) location.getYaw());
        config.set(path + "pitch", (double) location.getPitch());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + FILE_NAME + ": " + e.getMessage());
        }
    }
}
