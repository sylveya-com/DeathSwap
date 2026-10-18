package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@Setter
public class HideSettings {
  @Comment("Keep chat between players in the same match.")
  @JsonProperty("isolate-chat")
  private boolean isolateChat = true;

  @Comment("Keep death messages between players in the same match.")
  @JsonProperty("isolate-deaths")
  private boolean isolateDeaths = true;

  @Comment("Keep advancement messages between players in the same match.")
  @JsonProperty("isolate-achievements")
  private boolean isolateAchievements = true;

  @Comment("Hide players in a match from tab list of players outside that match.")
  @JsonProperty("match-players-in-tab")
  private boolean matchPlayersInTab = false;
}
