package dev.lokspel.deathswap.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

public final class SoundUtil {

    private SoundUtil() {}

    public static Sound minecraft(String key) {
        return Registry.SOUNDS.get(NamespacedKey.minecraft(key));
    }
}
