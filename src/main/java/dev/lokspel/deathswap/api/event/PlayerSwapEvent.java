package dev.lokspel.deathswap.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerSwapEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player first;
    private final Player second;

    @Setter
    private boolean cancelled;

    public PlayerSwapEvent(Player first, Player second) {
        this.first = first;
        this.second = second;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}