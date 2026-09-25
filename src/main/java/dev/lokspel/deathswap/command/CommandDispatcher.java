package dev.lokspel.deathswap.command;

import dev.lokspel.deathswap.config.MainConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandDispatcher implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final MainConfig config;

    public CommandDispatcher(MainConfig config, List<RegisteredCommand> commands) {
        this.config = config;
        for (RegisteredCommand cmd : commands) {
            subCommands.put(cmd.name(), cmd.executor());
        }
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(config.messages().prefixed("usage"));
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.execute(sender, args);
        }

        sender.sendMessage(config.messages().prefixed("usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub instanceof TabCompleter completer) {
            return completer.onTabComplete(sender, command, alias, args);
        }

        return List.of();
    }

    public record RegisteredCommand(String name, SubCommand executor) {}
}
