package dev.lokspel.deathswap.config.backed;

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
public class BackedConfig {

  @Comment("Plugin lobby location.")
  @JsonProperty("lobby")
  private LobbyEntry lobby = new LobbyEntry();
}
