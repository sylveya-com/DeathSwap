package dev.lokspel.deathswap.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
@Setter
public class WorldsSettings {
  @Comment("Number of persistent reusable worlds (concurrent matches).")
  @JsonProperty("count")
  private int count = 5;

  @Comment("Generate a fresh world when a game starts instead of reusing a pool.")
  @JsonProperty("generate-on-start")
  private boolean generateOnStart = true;

  @Comment("Pre-generate spawn chunks around the spawn, 0 = off.")
  @JsonProperty("pre-generate-radius")
  private int preGenerateRadius = 7;

  @Comment("Per-world random respawn radius around spawn, 0 = exact spawn.")
  @JsonProperty("spawn-radius")
  private int spawnRadius = 100;

  @Comment("World border size in blocks centered on spawn, 0 = disabled.")
  @JsonProperty("border")
  private int border = 3000;

  @Comment("Prefix for generated world names.")
  @JsonProperty("name-prefix")
  private String namePrefix = "ds";

  @Comment("Generate separate nether and end for each game world.")
  @JsonProperty("generate-dimensions")
  private boolean generateDimensions = true;
}
