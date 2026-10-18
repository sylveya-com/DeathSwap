package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand implements SubCommand {

    public SetLobbyCommand(DeathSwap ignored) {
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, messages.getCommands().getPlayerOnly());
            return true;
        }
        if (!sender.hasPermission("deathswap.setlobby")) {
            MessageUtil.send(sender, messages.getCommands().getNoPermission());
            return true;
        }
        DeathSwapConfig.backed().getLobby().setFromLocation(player.getLocation());
        DeathSwapConfig.saveBacked();
        MessageUtil.send(sender, messages.getCommands().getLobbySet());
        return true;
    }
}
