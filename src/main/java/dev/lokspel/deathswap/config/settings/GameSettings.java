package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@Setter
public class GameSettings {
  @Comment("Minimum players needed for the countdown to begin.")
  @JsonProperty("min-players-to-start")
  private int minPlayersToStart = 2;

  @Comment("Lobby countdown duration in seconds.")
  @JsonProperty("start-delay")
  private int startDelay = 120;

  @Comment("Player count at which the countdown shrinks.")
  @JsonProperty("min-players-fast-start")
  private int minPlayersFastStart = 4;

  @Comment("Shortened countdown duration in seconds.")
  @JsonProperty("fast-start-delay")
  private int fastStartDelay = 20;

  @Comment("Seconds between position swaps.")
  @JsonProperty("swap-interval")
  private int swapInterval = 300;

  @Comment("Warning countdown before each swap in seconds.")
  @JsonProperty("countdown-seconds")
  private int countdownSeconds = 5;

  @Comment("Maximum deaths before elimination.")
  @JsonProperty("max-deaths")
  private int maxDeaths = 5;

  @Comment("Maximum match duration in minutes, 0 = unlimited.")
  @JsonProperty("max-match-time")
  private int maxMatchTime = 0;

  @Comment("Whether players can damage each other.")
  @JsonProperty("pvp-enabled")
  private boolean pvpEnabled = false;
}
