package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.util.MessageUtil;
import dev.lokspel.deathswap.util.SoftDependUtil;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    private final DeathSwap plugin;

    public ReloadCommand(DeathSwap plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var messages = DeathSwapConfig.messages();
        if (!sender.hasPermission("deathswap.reload")) {
            MessageUtil.send(sender, messages.getCommands().getNoPermission());
            return true;
        }
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
            plugin.getLogger().warning("Failed to reload configuration: " + DeathSwapConfig.describeFailure(e));
            sender.sendMessage("§cFailed to reload configuration: " + e.getMessage());
        }
        return true;
    }
}
