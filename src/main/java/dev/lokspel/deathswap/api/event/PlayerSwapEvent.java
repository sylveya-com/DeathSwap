package dev.lokspel.deathswap.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class PlayerSwapEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Player> players;
    private final Player first;
    private final Player second;

    @Setter
    private boolean cancelled;

    public PlayerSwapEvent(Player first, Player second) {
        this.players = List.of(first, second);
        this.first = first;
        this.second = second;
    }

    public PlayerSwapEvent(List<Player> players) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException("A swap requires at least two players");
        }
        this.players = Collections.unmodifiableList(new ArrayList<>(players));
        this.first = this.players.get(0);
        this.second = this.players.get(1);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}