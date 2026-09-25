package dev.lokspel.deathswap.listener;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.game.MatchManager;
import dev.lokspel.deathswap.util.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class PlayerAdvancementDoneListener implements Listener {

    private final DeathSwap plugin;
    private final GameManager game;

    private final Method getMessageMethod;
    private final Method setMessageMethod;
    private final Method sendMessageMethod;

    public PlayerAdvancementDoneListener(DeathSwap plugin) {
        this.plugin = plugin;
        this.game = plugin.getGameManager();

        Class<?> component = ReflectionUtil.classOrNull(
                new StringBuilder()
                        .append("net")
                        .append(".kyori.adventure.text.Component")
                        .toString()
        );

        this.getMessageMethod = ReflectionUtil.methodOrNull(
                PlayerAdvancementDoneEvent.class, "message"
        );

        this.setMessageMethod = component == null ? null : ReflectionUtil.methodOrNull(
                PlayerAdvancementDoneEvent.class, "message", component
        );

        this.sendMessageMethod = component == null ? null : ReflectionUtil.methodOrNull(
                Player.class, "sendMessage", component
        );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handle(PlayerAdvancementDoneEvent event) {
        if (!plugin.getMainConfig().hide().isolateAchievements()
                || getMessageMethod == null
                || setMessageMethod == null
                || sendMessageMethod == null) {
            return;
        }

        MatchManager match = game.findMatchByPlayer(
                event.getPlayer().getUniqueId()
        );

        if (match == null) {
            return;
        }

        try {
            Object message = getMessageMethod.invoke(event);

            if (message == null) {
                return;
            }

            setMessageMethod.invoke(event, new Object[]{null});

            List<Player> recipients = new ArrayList<>(match.getOnlinePlayers());

            for (Player recipient : recipients) {
                sendMessageMethod.invoke(recipient, message);
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            plugin.getLogger().warning(
                    "Failed to isolate advancement announcement: "
                            + exception.getMessage()
            );
        }
    }
}