package dev.lokspel.deathswap.util;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public enum PluginPermission {

    START_CMD(new Permission("deathswap.start", PermissionDefault.OP)),
    STOP_CMD(new Permission("deathswap.stop", PermissionDefault.OP)),
    RELOAD_CMD(new Permission("deathswap.reload", PermissionDefault.OP)),
    SET_LOBBY_CMD(new Permission("deathswap.set.lobby", PermissionDefault.OP));

    private final Permission permission;

    PluginPermission(Permission permission) {
        this.permission = permission;
    }

    public Permission get() {
        return permission;
    }

    public static void registerAll() {
        for (PluginPermission pluginPermission : PluginPermission.values()) {
            try {
                Bukkit.getPluginManager().addPermission(pluginPermission.get());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static void unregisterAll() {
        for (PluginPermission pluginPermission : PluginPermission.values()) {
            try {
                Bukkit.getPluginManager().removePermission(pluginPermission.get());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
