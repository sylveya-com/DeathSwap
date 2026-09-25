package dev.lokspel.deathswap.util;

import org.bukkit.Bukkit;

public final class SoftDependUtil {

    private SoftDependUtil() {}

    public static final boolean PLACEHOLDER_API_ENABLED;
    public static final boolean PACKET_EVENTS_ENABLED;

    static {
        PLACEHOLDER_API_ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        PACKET_EVENTS_ENABLED = Bukkit.getPluginManager().getPlugin("packetevents") != null;
    }
}