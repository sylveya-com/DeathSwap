package dev.lokspel.deathswap.commands.deathswap.subcommands.set;

import dev.lokspel.deathswap.commands.SubCommand;
import dev.lokspel.deathswap.config.DeathSwapConfig;
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

public class SetLobbySubCmd implements SubCommand {

    @Override
    public @NotNull String label() {
        return "lobby";
    }

    @Override
    public @NotNull TextComponent syntax() {
        return Component.text("/deathswap set lobby").color(NamedTextColor.GOLD);
    }

    @Override
    public @NotNull TextComponent description() {
        return Component.text("Set the lobby location.").color(NamedTextColor.GRAY);
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
        if (!sender.hasPermission(PluginPermission.SET_LOBBY_CMD.get())) {
            MessageUtil.send(sender, messages.getCommands().getNoPermission());
            return true;
        }
        DeathSwapConfig.backed().getLobby().setFromLocation(player.getLocation());
        DeathSwapConfig.saveBacked();
        MessageUtil.send(sender, messages.getCommands().getLobbySet());
        return true;
    }
}
