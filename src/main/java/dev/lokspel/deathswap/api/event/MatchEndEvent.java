package dev.lokspel.deathswap.api.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class MatchEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player winner;

    /**
     * @param winner the surviving player, or {@code null} if the match ended with
     *               no winner (e.g. stopped or everyone disconnected)
     */
    public MatchEndEvent(@Nullable Player winner) {
        this.winner = winner;
    }

    public boolean hasWinner() {
        return winner != null;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}