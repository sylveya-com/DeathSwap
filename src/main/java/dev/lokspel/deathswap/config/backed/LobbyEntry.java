package dev.lokspel.deathswap.config.backed;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
@Getter
@Setter
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class LobbyEntry {

  @Comment("World name where the lobby is located.")
  @JsonProperty("world")
  private String world = "";

  @JsonProperty("x")
  private double x = 0.0;

  @JsonProperty("y")
  private double y = 64.0;

  @JsonProperty("z")
  private double z = 0.0;

  @JsonProperty("yaw")
  private float yaw = 0.0f;

  @JsonProperty("pitch")
  private float pitch = 0.0f;

  public boolean isSet() {
    return world != null && !world.isBlank();
  }

  public Location toLocation() {
    if (!isSet()) {
      return null;
    }
    World bukkitWorld = Bukkit.getWorld(world);
    if (bukkitWorld == null) {
      return null;
    }
    return new Location(bukkitWorld, x, y, z, yaw, pitch);
  }

  public void setFromLocation(Location location) {
    if (location == null || location.getWorld() == null) {
      return;
    }
    this.world = location.getWorld().getName();
    this.x = location.getX();
    this.y = location.getY();
    this.z = location.getZ();
    this.yaw = location.getYaw();
    this.pitch = location.getPitch();
  }
}
