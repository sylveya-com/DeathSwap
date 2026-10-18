package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {

    private final GameManager game;

    public LeaveCommand(DeathSwap plugin) {
        this.game = plugin.getGameManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, messages.getCommands().getPlayerOnly());
            return true;
        }
        if (!game.isParticipant(player)) {
            MessageUtil.send(sender, messages.getGame().getNotJoined());
            return true;
        }
        game.leave(player);
        return true;
    }
}
