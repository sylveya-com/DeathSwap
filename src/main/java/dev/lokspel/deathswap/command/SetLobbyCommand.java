package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.MainConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand implements SubCommand {

    private final MainConfig config;

    public SetLobbyCommand(DeathSwap plugin) {
        this.config = plugin.getMainConfig();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.messages().get("player-only"));
            return true;
        }
        if (!sender.hasPermission("deathswap.setlobby")) {
            sender.sendMessage(config.messages().get("no-permission"));
            return true;
        }
        config.lobby().set(player.getLocation());
        sender.sendMessage(config.messages().prefixed("lobby-set"));
        return true;
    }
}
