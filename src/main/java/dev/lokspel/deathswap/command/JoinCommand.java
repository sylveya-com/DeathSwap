package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements SubCommand {

    private final GameManager game;

    public JoinCommand(DeathSwap plugin) {
        this.game = plugin.getGameManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, DeathSwapConfig.messages().getCommands().getPlayerOnly());
            return true;
        }
        game.join(player);
        return true;
    }
}
