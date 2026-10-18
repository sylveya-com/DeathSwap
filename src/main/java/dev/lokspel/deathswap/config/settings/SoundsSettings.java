package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@Setter
public class SoundsSettings {
  @Comment("Sound played during countdown ticks.")
  @JsonProperty("countdown-tick")
  private String countdownTick = "entity.note.pling";

  @Comment("Sound played on countdown completion.")
  @JsonProperty("countdown-go")
  private String countdownGo = "entity.experience_orb.pickup";

  @Comment("Sound played when position swap occurs.")
  @JsonProperty("swap")
  private String swap = "entity.enderman.teleport";

  @Comment("Sound played when a player wins.")
  @JsonProperty("win")
  private String win = "ui.toast.challenge_complete";
}
