package dev.lokspel.deathswap.util.entityhider;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.game.MatchManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

/**
 * Keeps entity and tab-list visibility consistent with match membership.
 *
 * <p>A player inside a match is visible only to the other members of that same
 * match; everyone else sees them hidden. When
 * {@code hide.match-players-in-tab} is enabled the hidden player is also
 * removed from the observer's tab list, otherwise only their entity is hidden.
 * </p>
 */
public class PlayerHider implements Listener {

    private final DeathSwap plugin;

    public PlayerHider(DeathSwap plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        refreshLater();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            refreshLater();
        }
    }

    /**
     * Recomputes the visibility of every online player from scratch, based on
     * their current match membership.
     */
    public void refreshVisibility() {
        List<Player> online = List.copyOf(Bukkit.getOnlinePlayers());

        for (Player viewer : online) {
            MatchManager viewerMatch = matchOf(viewer);

            for (Player target : online) {
                if (viewer == target) {
                    continue;
                }

                if (isVisibleTo(viewerMatch, target)) {
                    showPlayer(viewer, target);
                    setListed(viewer, target, true);
                } else {
                    hidePlayer(viewer, target);
                    if (plugin.getMainConfig().hide().matchPlayersInTab()) {
                        setListed(viewer, target, false);
                    }
                }
            }
        }
    }

    private void refreshLater() {
        Bukkit.getScheduler().runTaskLater(plugin, this::refreshVisibility, 2L);
    }

    private boolean isVisibleTo(MatchManager viewerMatch, Player target) {
        MatchManager targetMatch = matchOf(target);

        if (viewerMatch == null) {
            return targetMatch == null;
        }

        return targetMatch == viewerMatch;
    }

    private MatchManager matchOf(Player player) {
        GameManager game = plugin.getGameManager();
        return game.findMatchByPlayer(player.getUniqueId());
    }

    private void showPlayer(Player viewer, Player target) {
        if (!viewer.canSee(target)) {
            viewer.showPlayer(plugin, target);
        }
    }

    private void hidePlayer(Player viewer, Player target) {
        if (viewer.canSee(target)) {
            viewer.hidePlayer(plugin, target);
        }
    }

    private void setListed(Player viewer, Player target, boolean listed) {
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info =
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(target.getUniqueId());
        info.setListed(listed);
        WrapperPlayServerPlayerInfoUpdate update =
                new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED, info);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, update);
    }
}