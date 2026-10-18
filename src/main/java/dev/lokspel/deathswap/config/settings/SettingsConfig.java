package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class SettingsConfig {

  @Comment("Core game match rules and intervals.")
  @JsonProperty("game")
  private GameSettings game = new GameSettings();

  @Comment("HUD display elements (scoreboard, actionbar countdowns).")
  @JsonProperty("display")
  private DisplaySettings display = new DisplaySettings();

  @Comment("Player and chat isolation between concurrent matches.")
  @JsonProperty("hide")
  private HideSettings hide = new HideSettings();

  @Comment("Sound events played during the match.")
  @JsonProperty("sounds")
  private SoundsSettings sounds = new SoundsSettings();

  @Comment("Game world generation and pool settings.")
  @JsonProperty("worlds")
  private WorldsSettings worlds = new WorldsSettings();

  @Comment("Commands executed when a match ends.")
  @JsonProperty("commands")
  private CommandsSettings commands = new CommandsSettings();
}
