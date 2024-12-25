package dev.demeng.frost.user.player;

import dev.demeng.frost.user.effects.SpecialEffects;
import java.util.Arrays;
import lombok.Data;
import lombok.Getter;

@Data
public class PlayerSettings {

  private boolean duelRequests = true;
  private boolean partyInvites = true;
  private boolean playerVisibility = false;
  private boolean spectatorsAllowed = true;

  private boolean vanillaTab = false;
  private boolean scoreboardToggled = true;
  private boolean pingScoreboardToggled = true;

  private boolean clearInventory = false;
  private boolean bodyAnimation = false;
  private boolean startFlying = false;

  private int pingRange = -1;

  private SpecialEffects specialEffect = SpecialEffects.NONE;
  private PlayerTime playerTime = PlayerTime.DAY;

  @Getter
  public enum PlayerTime {

    DAY("Day", 4000L),
    SUNSET("Sunset", 12000L),
    NIGHT("Night", 20000L);

    private final String name;
    private final long time;

    PlayerTime(String name, long time) {
      this.name = name;
      this.time = time;
    }

    public static PlayerTime getByName(String input) {
      return Arrays.stream(values()).filter(
              (type) -> type.name().equalsIgnoreCase(input) || type.getName().equalsIgnoreCase(input))
          .findFirst().orElse(null);
    }
  }
}
