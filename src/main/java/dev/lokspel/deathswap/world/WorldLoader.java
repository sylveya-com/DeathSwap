package dev.lokspel.deathswap.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

/**
 * Creates or loads a game world by name and environment. Must be called on the
 * main thread: {@code Bukkit.createWorld} requires it. Modern Paper no longer
 * pre-generates a large spawn area synchronously, so world creation is cheap;
 * the spawn region is pre-generated asynchronously afterward.
 *
 * <p>Each game world gets its own {@code _nether} and {@code _the_end}
 * companion worlds using the standard Minecraft naming convention, so portals
 * built in a match link to that match's dimensions instead of the server's
 * shared ones.
 *
 * <p>The exact spawn position is corrected afterward in
 * {@code WorldPool#loadInstance}.
 */
final class WorldLoader {

    World load(String name, World.Environment environment) {
        WorldCreator creator = new WorldCreator(name)
                .environment(environment);
        return Bukkit.createWorld(creator);
    }
}