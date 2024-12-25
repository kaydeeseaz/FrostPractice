package dev.demeng.frost.util;

import java.util.EnumMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.EntityType;

@UtilityClass
public class EntityUtils {

  private final Map<EntityType, String> displayNames = new EnumMap<>(EntityType.class);
  private int currentFakeEntityId = -1;

  public String getName(EntityType type) {
    return displayNames.get(type);
  }

  public int getFakeEntityId() {
    return currentFakeEntityId--;
  }

  static {
    displayNames.put(EntityType.EGG, "Egg");
    displayNames.put(EntityType.ARROW, "Arrow");
    displayNames.put(EntityType.PLAYER, "Player");
    displayNames.put(EntityType.FIREWORK, "Firework");
    displayNames.put(EntityType.SNOWBALL, "Snowball");
    displayNames.put(EntityType.DROPPED_ITEM, "Item");
    displayNames.put(EntityType.SPLASH_POTION, "Potion");
    displayNames.put(EntityType.FISHING_HOOK, "Fishing Rod Hook");
    displayNames.put(EntityType.EXPERIENCE_ORB, "Experience Orb");
  }
}
