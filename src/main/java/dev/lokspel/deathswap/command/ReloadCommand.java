package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.MainConfig;
import dev.lokspel.deathswap.util.SoftDependUtil;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    private final DeathSwap plugin;
    private final MainConfig config;

    public ReloadCommand(DeathSwap plugin) {
        this.plugin = plugin;
        this.config = plugin.getMainConfig();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("deathswap.reload")) {
            sender.sendMessage(config.messages().get("no-permission"));
            return true;
        }
        config.load();
        if (SoftDependUtil.PACKET_EVENTS_ENABLED && plugin.getPlayerHider() != null) {
            plugin.getPlayerHider().refreshVisibility();
        }
        sender.sendMessage(config.messages().prefixed("config-reloaded"));
        return true;
    }
}
