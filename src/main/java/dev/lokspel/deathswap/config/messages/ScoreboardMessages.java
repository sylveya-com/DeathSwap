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
public class ScoreboardMessages {
  @JsonProperty("title")
  private String title = "<gold><bold>DeathSwap";

  @JsonProperty("entry")
  private String entry = "<white>{player}";
}
