package dev.lokspel.deathswap.util;

import dev.lokspel.deathswap.config.DeathSwapConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class MessageUtil {

  private static final MiniMessage MINI = MiniMessage.miniMessage();

  private static final LegacyComponentSerializer LEGACY =
          LegacyComponentSerializer.legacySection();

  private MessageUtil() {}

  public static String format(String message) {
    if (message == null || message.isEmpty()) {
      return "";
    }

    String prefix = DeathSwapConfig.messages().getPrefix();

    String resolved = message
            .replace("{prefix}", prefix != null ? prefix : "")
            .replace("%prefix%", prefix != null ? prefix : "");

    Component component = MINI.deserialize(resolved);
    return LEGACY.serialize(component);
  }

  public static String format(String message, String placeholder, Object value) {
    return format(replace(message, placeholder, value));
  }

  public static String format(
          String message,
          String p1, Object v1,
          String p2, Object v2
  ) {
    return format(replace(replace(message, p1, v1), p2, v2));
  }

  private static String replace(String message, String placeholder, Object value) {
    if (message == null || placeholder == null) {
      return message;
    }

    String val = String.valueOf(value);

    return message
            .replace("{" + placeholder + "}", val)
            .replace("<" + placeholder + ">", val)
            .replace("%" + placeholder + "%", val);
  }

  public static void send(CommandSender sender, String message) {
    if (sender == null || message == null || message.isEmpty()) {
      return;
    }

    sender.sendMessage(format(message));
  }

  public static void send(
          CommandSender sender,
          String message,
          String placeholder,
          Object value
  ) {
    if (sender == null || message == null || message.isEmpty()) {
      return;
    }

    sender.sendMessage(format(message, placeholder, value));
  }

  public static void broadcast(
          Collection<? extends Player> players,
          String message
  ) {
    if (players == null || message == null || message.isEmpty()) {
      return;
    }

    String formatted = format(message);

    for (Player player : players) {
      if (player != null && player.isOnline()) {
        player.sendMessage(formatted);
      }
    }
  }
}