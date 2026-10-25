package dev.lokspel.deathswap.commands.deathswap.subcommands;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.commands.SubCommand;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.MessageUtil;
import dev.lokspel.deathswap.util.PluginPermission;
import dev.lokspel.deathswap.util.SoftDependUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ReloadSubCmd implements SubCommand {

    @Override
    public @NotNull String label() {
        return "reload";
    }

    @Override
    public @NotNull TextComponent syntax() {
        return Component.text("/deathswap reload").color(NamedTextColor.GOLD);
    }

    @Override
    public @NotNull TextComponent description() {
        return Component.text("Reload the plugin configuration.").color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws CommandException, IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!sender.hasPermission(PluginPermission.RELOAD_CMD.get())) {
            MessageUtil.send(sender, messages.getCommands().getNoPermission());
            return true;
        }
        DeathSwap plugin = DeathSwap.getInstance();
        try {
            DeathSwapConfig.reload();
            if (SoftDependUtil.PACKET_EVENTS_ENABLED && plugin.getPlayerHider() != null) {
                plugin.getPlayerHider().refreshVisibility();
            }
            if (plugin.getWorldPool() != null) {
                plugin.getWorldPool().reload();
            }
            MessageUtil.send(sender, DeathSwapConfig.messages().getCommands().getConfigReloaded());
        } catch (Exception e) {
            MessageUtil.send(sender, "<red>Failed to reload configuration: " + DeathSwapConfig.describeFailure(e));
        }
        return true;
    }
}
