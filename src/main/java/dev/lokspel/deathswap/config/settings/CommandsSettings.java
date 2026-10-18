package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@Setter
public class CommandsSettings {
  @Comment("Commands run when a player returns to the lobby after match end.")
  @JsonProperty("on-match-end")
  private List<CommandEntry> onMatchEnd = new ArrayList<>();
}
