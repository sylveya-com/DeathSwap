package dev.lokspel.deathswap.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class PlayerUtil {

    private PlayerUtil() {
    }

    public static Set<Player> getOnlinePlayers(Set<UUID> uuids) {
        var online = new java.util.HashSet<Player>();
        for (UUID uuid : uuids) {
            Player player = getOnlinePlayer(uuid);
            if (player != null) online.add(player);
        }
        return online;
    }

    public static Player getOnlinePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return (player != null && player.isOnline()) ? player : null;
    }

    public static void showCountdownTitle(Player player, String message) {
        player.sendTitle(
                message,
                "",
                0,
                20,
                0
        );
    }

    public static void showActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}