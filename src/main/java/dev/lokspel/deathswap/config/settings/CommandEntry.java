package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = CommandEntry.Deserializer.class)
public class CommandEntry {
  @JsonProperty("command")
  private String command = "";

  @JsonProperty("bypass-permissions")
  private List<String> bypassPermissions = new ArrayList<>();

  public String command() {
    return command;
  }

  public List<String> bypassPermissions() {
    return bypassPermissions != null ? bypassPermissions : List.of();
  }

  public static class Deserializer extends JsonDeserializer<CommandEntry> {
    @Override
    public CommandEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.currentToken() == JsonToken.VALUE_STRING) {
        return new CommandEntry(p.getText(), new ArrayList<>());
      }
      JsonNode node = p.readValueAsTree();
      String command = node.has("command") ? node.get("command").asText() : "";
      List<String> bypassPermissions = new ArrayList<>();
      if (node.has("bypass-permissions") && node.get("bypass-permissions").isArray()) {
        for (JsonNode perm : node.get("bypass-permissions")) {
          bypassPermissions.add(perm.asText());
        }
      }
      return new CommandEntry(command, bypassPermissions);
    }
  }
}
