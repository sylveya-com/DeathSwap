package dev.lokspel.deathswap.config.section;

import dev.lokspel.deathswap.config.BackedConfig;
import org.bukkit.Location;

public class LobbySection {

    private static final String NAME = "lobby";

    private final BackedConfig backed;

    public LobbySection(BackedConfig backed) {
        this.backed = backed;
    }

    public void set(Location location) {
        backed.setLocation(NAME, location);
    }

    public boolean isNotSet() {
        return !backed.isSet(NAME);
    }

    public Location get() {
        return backed.location(NAME);
    }
}
