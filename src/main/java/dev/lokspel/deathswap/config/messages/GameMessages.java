package dev.lokspel.deathswap.config.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class GameMessages {
  @JsonProperty("joined")
  private String joined = "{prefix}<green>You joined the game.";

  @JsonProperty("already-joined")
  private String alreadyJoined = "{prefix}<red>You are already in the game.";

  @JsonProperty("already-queue")
  private String alreadyQueue = "{prefix}<yellow>You are already waiting for a game.";

  @JsonProperty("not-joined")
  private String notJoined = "{prefix}<red>You are not in the game.";

  @JsonProperty("left")
  private String left = "{prefix}<gray>You left the game.";

  @JsonProperty("not-enough-players")
  private String notEnoughPlayers = "{prefix}<red>Not enough players to start.";

  @JsonProperty("worlds-busy")
  private String worldsBusy = "{prefix}<red>All worlds are currently in use. Please wait.";

  @JsonProperty("world-preparing")
  private String worldPreparing = "{prefix}<yellow>Preparing world, please wait...";

  @JsonProperty("started")
  private String started = "{prefix}<green>Game has started! Good luck!";

  @JsonProperty("already-running")
  private String alreadyRunning = "{prefix}<red>A game is already running.";

  @JsonProperty("starting")
  private String starting = "<yellow>Game starts in <gold>{seconds}</gold> seconds.";

  @JsonProperty("needs-players")
  private String needsPlayers = "<yellow>Waiting for players: <gold>{current}</gold>/<gold>{needed}</gold>";

  @JsonProperty("countdown")
  private String countdown = "<yellow>Swap in <gold>{seconds}</gold> seconds!";

  @JsonProperty("swapped")
  private String swapped = "{prefix}<yellow>Swapped positions!";

  @JsonProperty("eliminated")
  private String eliminated = "{prefix}<red>You have been eliminated!";

  @JsonProperty("player-eliminated")
  private String playerEliminated = "{prefix}<gold>{player}</gold> <red>has been eliminated!";

  @JsonProperty("deaths-left")
  private String deathsLeft = "{prefix}<yellow>Deaths remaining: <gold>{deaths}</gold>";

  @JsonProperty("winner")
  private String winner = "{prefix}<gold>{player}</gold> <green>won the game!";

  @JsonProperty("time-up")
  private String timeUp = "{prefix}<yellow>Time limit reached! Game over.";

  @JsonProperty("stopped")
  private String stopped = "{prefix}<red>Game was stopped.";
}
