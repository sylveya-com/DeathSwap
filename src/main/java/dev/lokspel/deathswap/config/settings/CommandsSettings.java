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
  @Comment({
      "Commands run after a player leaves a match and returns to the lobby.",
      "Prefix with [player] to run as player or [console] for console (default).",
      "Placeholders: %player%, %uuid%.",
      "",
      "Example:",
      "  on-match-end:",
      "    - command: \"[player] spawn\"",
      "      bypass-permissions:",
      "        - \"essentials.spawn\"",
      "    - command: \"[console] say Match ended for %player%\""
  })
  @JsonProperty("on-match-end")
  private List<CommandEntry> onMatchEnd = new ArrayList<>();
}
