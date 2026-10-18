package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.util.MessageUtil;
import org.bukkit.command.CommandSender;

public class StartCommand implements SubCommand {

    private final GameManager game;

    public StartCommand(DeathSwap plugin) {
        this.game = plugin.getGameManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!sender.hasPermission("deathswap.start")) {
            MessageUtil.send(sender, messages.getCommands().getNoPermission());
            return true;
        }
        if (!DeathSwapConfig.backed().getLobby().isSet()) {
            MessageUtil.send(sender, messages.getCommands().getLobbyNotSet());
            return true;
        }
        if (!game.forceStart()) {
            MessageUtil.send(sender, messages.getGame().getNotEnoughPlayers());
        }
        return true;
    }
}
