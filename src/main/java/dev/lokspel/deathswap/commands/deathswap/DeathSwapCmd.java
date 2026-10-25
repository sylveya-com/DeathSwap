package dev.lokspel.deathswap.commands.deathswap;

import com.google.common.collect.ImmutableList;
import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.commands.SubCommand;
import dev.lokspel.deathswap.commands.deathswap.subcommands.JoinSubCmd;
import dev.lokspel.deathswap.commands.deathswap.subcommands.LeaveSubCmd;
import dev.lokspel.deathswap.commands.deathswap.subcommands.ReloadSubCmd;
import dev.lokspel.deathswap.commands.deathswap.subcommands.SetSubCmd;
import dev.lokspel.deathswap.commands.deathswap.subcommands.StartSubCmd;
import dev.lokspel.deathswap.commands.deathswap.subcommands.StopSubCmd;
import dev.lokspel.deathswap.util.Enableable;
import dev.lokspel.deathswap.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeathSwapCmd extends Command implements Enableable {

    private final @NotNull List<SubCommand> subCommands;
    private final @NotNull List<String> tabCompletes;

    public DeathSwapCmd() {
        super(
                "deathswap",
                "Main DeathSwap command",
                "/deathswap [ start, stop, join, leave, reload, set ]",
                ImmutableList.of("ds")
        );
        subCommands = ImmutableList.of(
                new StartSubCmd(),
                new StopSubCmd(),
                new JoinSubCmd(),
                new LeaveSubCmd(),
                new ReloadSubCmd(),
                new SetSubCmd()
        );
        tabCompletes = subCommands.stream().map(SubCommand::label).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    @Override
    public void enable() {
        PluginCommand pluginCommand = DeathSwap.getInstance().getCommand(getName());
        if (pluginCommand != null) {
            pluginCommand.setExecutor((sender, command, label, args) -> execute(sender, label, args));
            pluginCommand.setTabCompleter((sender, command, alias, args) -> tabComplete(sender, alias, args));
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws CommandException, IllegalArgumentException {
        if (args.length == 1) {
            return tabCompletes;
        }

        if (args.length >= 2) {
            for (final SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.label())) {
                    return subCommand.tabComplete(sender, alias, args);
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length >= 1) {
            for (final SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.label())) {
                    return subCommand.execute(sender, commandLabel, args);
                }
            }
        }

        return commandOverview(sender);
    }

    private boolean commandOverview(CommandSender sender) {
        MessageUtil.send(sender, Component.text("-----------------------------------------------------", NamedTextColor.DARK_AQUA));
        MessageUtil.send(sender, Component.text("DeathSwap Commands", NamedTextColor.GOLD));
        MessageUtil.send(sender, Component.text("-----------------------------------------------------", NamedTextColor.DARK_AQUA));
        for (final SubCommand subCmd : subCommands) {
            MessageUtil.send(sender, subCmd.syntax().append(Component.text(" - ", NamedTextColor.GRAY)).append(subCmd.description()));
        }
        MessageUtil.send(sender, Component.text("-----------------------------------------------------", NamedTextColor.DARK_AQUA));
        return true;
    }
}
