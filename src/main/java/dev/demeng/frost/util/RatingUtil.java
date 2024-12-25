package dev.demeng.frost.util;

import org.bukkit.ChatColor;

public enum RatingUtil {

  NONE("Not Placed", ChatColor.DARK_GRAY, -2, 0),

  BRONZE_V("Bronze V", ChatColor.YELLOW, 900, 975),
  BRONZE_IV("Bronze IV", ChatColor.YELLOW, 975, 1050),
  BRONZE_III("Bronze III", ChatColor.YELLOW, 1050, 1125),
  BRONZE_II("Bronze II", ChatColor.YELLOW, 1125, 1200),
  BRONZE_I("Bronze I", ChatColor.YELLOW, 1200, 1275),

  SILVER_V("Silver V", ChatColor.GRAY, 1275, 1350),
  SILVER_IV("Silver IV", ChatColor.GRAY, 1350, 1425),
  SILVER_III("Silver III", ChatColor.GRAY, 1425, 1500),
  SILVER_II("Silver II", ChatColor.GRAY, 1500, 1575),
  SILVER_I("Silver I", ChatColor.GRAY, 1575, 1650),

  GOLD_V("Gold V", ChatColor.GOLD, 1650, 1725),
  GOLD_IV("Gold IV", ChatColor.GOLD, 1725, 1800),
  GOLD_III("Gold III", ChatColor.GOLD, 1800, 1875),
  GOLD_II("Gold II", ChatColor.GOLD, 1875, 1950),
  GOLD_I("Gold I", ChatColor.GOLD, 1950, 2025),

  EMERALD_V("Emerald V", ChatColor.GREEN, 2025, 2100),
  EMERALD_IV("Emerald IV", ChatColor.GREEN, 2100, 2175),
  EMERALD_III("Emerald III", ChatColor.GREEN, 2175, 2250),
  EMERALD_II("Emerald II", ChatColor.GREEN, 2250, 2325),
  EMERALD_I("Emerald I", ChatColor.GREEN, 2325, 2400),

  DIAMOND_V("Diamond V", ChatColor.AQUA, 2400, 2475),
  DIAMOND_IV("Diamond IV", ChatColor.AQUA, 2475, 2550),
  DIAMOND_III("Diamond III", ChatColor.AQUA, 2550, 2625),
  DIAMOND_II("Diamond II", ChatColor.AQUA, 2625, 2700),
  DIAMOND_I("Diamond I", ChatColor.AQUA, 2700, 2775),

  MASTERS("Masters", ChatColor.RED, 2775, 20000);

  private final String name;
  private final ChatColor chatColor;
  private final int minElo;
  private final int maxElo;

  RatingUtil(String name, ChatColor chatColor, int minElo, int maxElo) {
    this.name = name;
    this.chatColor = chatColor;
    this.minElo = minElo;
    this.maxElo = maxElo;
  }

  public static RatingUtil getRankByElo(double elo) {
    for (RatingUtil rank : RatingUtil.values()) {
      if (elo >= rank.minElo && elo < rank.maxElo) {
        return rank;
      }
    }

    return RatingUtil.NONE;
  }

  public static boolean isPromotionGame(int oldElo, int newElo) {
    return getRankByElo(newElo) != getRankByElo(oldElo);
  }

  public String getName() {
    return name;
  }

  public ChatColor getChatColor() {
    return this.chatColor;
  }
}
