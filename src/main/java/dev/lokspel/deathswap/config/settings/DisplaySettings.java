package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@Setter
public class DisplaySettings {
  @Comment("Show the in-match sidebar scoreboard.")
  @JsonProperty("scoreboard")
  private boolean scoreboard = true;

  @Comment("Show HUD countdowns on the action bar.")
  @JsonProperty("actionbar")
  private boolean actionbar = true;
}
