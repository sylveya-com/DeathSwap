package dev.lokspel.deathswap.config;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.section.DisplaySection;
import dev.lokspel.deathswap.config.section.GameSection;
import dev.lokspel.deathswap.config.section.HideSection;
import dev.lokspel.deathswap.config.section.LobbySection;
import dev.lokspel.deathswap.config.section.SoundsSection;
import dev.lokspel.deathswap.config.section.WorldsSection;

public class MainConfig {

    private final DeathSwap plugin;
    private final GameSection game;
    private final WorldsSection worlds;
    private final SoundsSection sounds;
    private final LobbySection lobby;
    private final HideSection hide;
    private final DisplaySection display;
    private MessagesConfig messages;

    public MainConfig(DeathSwap plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.game = new GameSection(plugin);
        this.worlds = new WorldsSection(plugin);
        this.sounds = new SoundsSection(plugin);
        this.lobby = new LobbySection(plugin);
        this.hide = new HideSection(plugin);
        this.display = new DisplaySection(plugin);
        this.messages = new MessagesConfig(plugin);
    }

    public void load() {
        plugin.reloadConfig();
        messages = new MessagesConfig(plugin);
    }

    public GameSection game() {
        return game;
    }

    public WorldsSection worlds() {
        return worlds;
    }

    public SoundsSection sounds() {
        return sounds;
    }

    public LobbySection lobby() {
        return lobby;
    }

    public HideSection hide() {
        return hide;
    }

    public DisplaySection display() {
        return display;
    }

    public MessagesConfig messages() {
        return messages;
    }
}
