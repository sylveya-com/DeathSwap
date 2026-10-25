package dev.lokspel.deathswap.commands.deathswap.subcommands;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.commands.SubCommand;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.util.MessageUtil;
import dev.lokspel.deathswap.util.PluginPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class StopSubCmd implements SubCommand {

    @Override
    public @NotNull String label() {
        return "stop";
    }

    @Override
    public @NotNull TextComponent syntax() {
        return Component.text("/deathswap stop").color(NamedTextColor.GOLD);
    }

    @Override
    public @NotNull TextComponent description() {
        return Component.text("Stop the running match.").color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws CommandException, IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!sender.hasPermission(PluginPermission.STOP_CMD.get())) {
            MessageUtil.send(sender, messages.getCommands().getNoPermission());
            return true;
        }
        GameManager game = DeathSwap.getInstance().getGameManager();
        if (!game.hasActivity()) {
            MessageUtil.send(sender, messages.getCommands().getNotRunning());
            return true;
        }

        boolean wasParticipant = sender instanceof Player player && game.isParticipant(player);
        game.stop();
        if (!wasParticipant) {
            MessageUtil.send(sender, messages.getGame().getStopped());
        }
        return true;
    }
}
