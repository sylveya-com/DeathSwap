package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.MessagesConfig;
import dev.lokspel.deathswap.game.GameManager;
import org.bukkit.command.CommandSender;

public class StopCommand implements SubCommand {

    private final GameManager game;
    private final MessagesConfig messages;

    public StopCommand(DeathSwap plugin) {
        this.game = plugin.getGameManager();
        this.messages = plugin.getMainConfig().messages();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("deathswap.stop")) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }
        if (!game.hasActivity()) {
            sender.sendMessage(messages.prefixed("not-running"));
            return true;
        }
        game.stop();
        return true;
    }
}
