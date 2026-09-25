package dev.lokspel.deathswap.config;

import dev.lokspel.deathswap.DeathSwap;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class MessagesConfig {

    private static final String PREFIX = "prefix";
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessagesConfig(DeathSwap plugin) {
        this.config = load(plugin);
    }

    private FileConfiguration load(DeathSwap plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public String get(String key) {
        return parse(config.getString(key, ""));
    }

    public String get(String key, String placeholder, String value) {
        return parse(
                config.getString(key, ""),
                placeholder,
                value
        );
    }

    public String get(String key, String p1, String v1, String p2, String v2) {
        return parse(
                config.getString(key, "").replace("<" + p1 + ">", v1).replace("<" + p2 + ">", v2)
        );
    }

    public String prefixed(String key) {
        return parse(replacePrefix(config.getString(key, "")));
    }

    public String prefixed(String key, String placeholder, String value) {
        return parse(
                replacePrefix(config.getString(key, "")),
                placeholder,
                value
        );
    }

    private String replacePrefix(String text) {
        return text.replace("%prefix%", config.getString(PREFIX, ""));
    }

    private String parse(String text) {
        return LEGACY.serialize(miniMessage.deserialize(text));
    }

    private String parse(String text, String placeholder, String value) {
        return LEGACY.serialize(miniMessage.deserialize(
                text.replace("<" + placeholder + ">", value)
        ));
    }
}