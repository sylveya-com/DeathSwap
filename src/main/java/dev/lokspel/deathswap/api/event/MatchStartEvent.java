package dev.lokspel.deathswap.api.event;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class MatchStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Player> players;
    private final World world;

    public MatchStartEvent(List<Player> players, World world) {
        this.players = List.copyOf(players);
        this.world = world;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}