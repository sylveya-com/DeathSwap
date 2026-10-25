package dev.lokspel.deathswap.util;

import dev.lokspel.deathswap.config.DeathSwapConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class MessageUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private MessageUtil() {}

    public static void sendMessage(@NotNull CommandSender sender, @NotNull Component message) {
        sender.sendMessage(LEGACY.serialize(message));
    }

    public static void send(@NotNull CommandSender sender, @NotNull Component message) {
        sendMessage(sender, message);
    }

    public static void send(@NotNull CommandSender sender, @NotNull String message) {
        sendMessage(sender, parse(message));
    }

    public static void send(@NotNull CommandSender sender, @NotNull String message, @NotNull String key, @NotNull Object value) {
        send(sender, replace(message, key, String.valueOf(value)));
    }

    public static @NotNull Component parse(@NotNull String message) {
        String prefix = DeathSwapConfig.messages().getPrefix();
        String text = replaceAmpersand(message)
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("%prefix%", prefix != null ? prefix : "");
        return MiniMessage.miniMessage().deserialize(text);
    }

    public static @NotNull String format(@NotNull String message) {
        return LEGACY.serialize(parse(message));
    }

    public static @NotNull String format(@NotNull String message, @NotNull String key, @NotNull Object value) {
        return format(replace(message, key, String.valueOf(value)));
    }

    public static @NotNull String format(@NotNull String message, @NotNull String k1, @NotNull Object v1, @NotNull String k2, @NotNull Object v2) {
        return format(replace(replace(message, k1, String.valueOf(v1)), k2, String.valueOf(v2)));
    }

    public static void broadcast(@NotNull Collection<? extends Player> players, @NotNull Component message) {
        String serialized = LEGACY.serialize(message);
        for (Player player : players) {
            if (player != null && player.isOnline()) {
                player.sendMessage(serialized);
            }
        }
    }

    public static void broadcast(@NotNull Collection<? extends Player> players, @NotNull String message) {
        if (message.contains("§")) {
            for (Player player : players) {
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            }
            return;
        }
        broadcast(players, parse(message));
    }

    private static @NotNull String replace(@NotNull String message, @NotNull String key, @NotNull String value) {
        return message
                .replace("{" + key + "}", value)
                .replace("%" + key + "%", value)
                .replace("<" + key + ">", value);
    }

    public static @NotNull String replaceAmpersand(@NotNull String string) {
        return string
                .replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&k", "<obfuscated>").replace("&l", "<bold>")
                .replace("&m", "<strikethrough>").replace("&n", "<underlined>")
                .replace("&o", "<italic>").replace("&r", "<reset>");
    }
}
