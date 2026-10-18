package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.util.MessageUtil;
import org.bukkit.command.CommandSender;

public class StopCommand implements SubCommand {

    private final GameManager game;

    public StopCommand(DeathSwap plugin) {
        this.game = plugin.getGameManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!sender.hasPermission("deathswap.stop")) {
            MessageUtil.send(sender, messages.getCommands().getNoPermission());
            return true;
        }
        if (!game.hasActivity()) {
            MessageUtil.send(sender, messages.getCommands().getNotRunning());
            return true;
        }
        game.stop();
        return true;
    }
}
