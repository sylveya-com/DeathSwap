package dev.lokspel.deathswap.config.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@Setter
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class MessagesConfig {
  @Comment("Prefix prepended to messages using {prefix}.")
  @JsonProperty("prefix")
  private String prefix = "<gold>[DeathSwap]</gold> ";

  @JsonProperty("commands")
  private CommandMessages commands = new CommandMessages();

  @JsonProperty("game")
  private GameMessages game = new GameMessages();

  @JsonProperty("scoreboard")
  private ScoreboardMessages scoreboard = new ScoreboardMessages();
}
