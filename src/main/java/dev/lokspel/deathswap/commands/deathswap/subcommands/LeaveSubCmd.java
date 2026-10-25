package dev.lokspel.deathswap.commands.deathswap.subcommands;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.commands.SubCommand;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class LeaveSubCmd implements SubCommand {

    @Override
    public @NotNull String label() {
        return "leave";
    }

    @Override
    public @NotNull TextComponent syntax() {
        return Component.text("/deathswap leave").color(NamedTextColor.GOLD);
    }

    @Override
    public @NotNull TextComponent description() {
        return Component.text("Leave the current match.").color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws CommandException, IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, messages.getCommands().getPlayerOnly());
            return true;
        }
        GameManager game = DeathSwap.getInstance().getGameManager();
        if (!game.isParticipant(player)) {
            MessageUtil.send(sender, messages.getGame().getNotJoined());
            return true;
        }
        game.leave(player);
        return true;
    }
}
