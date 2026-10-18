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
public class CommandMessages {
  @JsonProperty("usage")
  private String usage = "{prefix}<red>Usage: /deathswap <join|leave|start|stop|reload|setlobby>";

  @JsonProperty("player-only")
  private String playerOnly = "<red>Only players can use this command.";

  @JsonProperty("no-permission")
  private String noPermission = "<red>You don't have permission to use this command.";

  @JsonProperty("lobby-set")
  private String lobbySet = "{prefix}<green>Lobby location has been set.";

  @JsonProperty("lobby-not-set")
  private String lobbyNotSet = "{prefix}<red>Lobby is not set. Use /deathswap setlobby first.";

  @JsonProperty("config-reloaded")
  private String configReloaded = "{prefix}<green>Configuration reloaded successfully.";

  @JsonProperty("not-running")
  private String notRunning = "{prefix}<red>No game is currently running.";
}
