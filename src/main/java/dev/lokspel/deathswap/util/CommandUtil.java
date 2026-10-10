package dev.lokspel.deathswap.util;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.section.CommandsSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.List;

public final class CommandUtil {

    private static final String PLAYER_PREFIX = "[player]";
    private static final String CONSOLE_PREFIX = "[console]";

    private CommandUtil() {
    }

    public static void run(List<CommandsSection.Entry> entries, Player player) {
        if (entries.isEmpty()) {
            return;
        }
        Bukkit.getScheduler().runTask(DeathSwap.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }
            for (CommandsSection.Entry entry : entries) {
                String command = entry.command().trim();
                boolean asPlayer;

                if (regionMatchesIgnoreCase(command, PLAYER_PREFIX)) {
                    asPlayer = true;
                    command = command.substring(PLAYER_PREFIX.length()).trim();
                } else if (regionMatchesIgnoreCase(command, CONSOLE_PREFIX)) {
                    asPlayer = false;
                    command = command.substring(CONSOLE_PREFIX.length()).trim();
                } else {
                    asPlayer = false;
                }

                command = command
                        .replace("%player%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .trim();
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                if (command.isEmpty()) {
                    continue;
                }

                if (asPlayer) {
                    performAs(player, command, entry.bypassPermissions());
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        });
    }

    private static void performAs(Player player, String command, List<String> bypassPermissions) {
        if (bypassPermissions.isEmpty()) {
            player.performCommand(command);
            return;
        }

        List<PermissionAttachment> attachments = new ArrayList<>();
        try {
            for (String node : bypassPermissions) {
                attachments.add(player.addAttachment(DeathSwap.getInstance(), node, true));
            }
            player.performCommand(command);
        } finally {
            for (PermissionAttachment attachment : attachments) {
                try {
                    player.removeAttachment(attachment);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private static boolean regionMatchesIgnoreCase(String value, String prefix) {
        return value.length() >= prefix.length()
                && value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
