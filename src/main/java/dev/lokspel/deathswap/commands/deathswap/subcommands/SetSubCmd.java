package dev.lokspel.deathswap.commands.deathswap.subcommands;

import com.google.common.collect.ImmutableList;
import dev.lokspel.deathswap.commands.SubCommand;
import dev.lokspel.deathswap.commands.deathswap.subcommands.set.SetLobbySubCmd;
import dev.lokspel.deathswap.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetSubCmd implements SubCommand {

    private final @NotNull List<SubCommand> subCommands;
    private final @NotNull List<String> tabCompletes;

    public SetSubCmd() {
        this.subCommands = ImmutableList.of(new SetLobbySubCmd());
        this.tabCompletes = subCommands.stream().map(SubCommand::label).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    @Override
    public @NotNull String label() {
        return "set";
    }

    @Override
    public @NotNull TextComponent syntax() {
        return Component.text("/deathswap set <" + String.join("|", tabCompletes) + ">").color(NamedTextColor.GOLD);
    }

    @Override
    public @NotNull TextComponent description() {
        return Component.text("Set game locations.").color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws CommandException, IllegalArgumentException {
        if (args.length == 2) {
            String current = args[1].toLowerCase();
            return tabCompletes.stream()
                    .filter(s -> s.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        }

        if (args.length >= 3) {
            for (final SubCommand subCommand : subCommands) {
                if (args[1].equalsIgnoreCase(subCommand.label())) {
                    return subCommand.tabComplete(sender, alias, args);
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length >= 2) {
            for (final SubCommand subCommand : subCommands) {
                if (args[1].equalsIgnoreCase(subCommand.label())) {
                    return subCommand.execute(sender, commandLabel, args);
                }
            }
        }

        MessageUtil.send(sender, Component.text("Usage: ", NamedTextColor.RED).append(syntax()));
        return true;
    }
}
