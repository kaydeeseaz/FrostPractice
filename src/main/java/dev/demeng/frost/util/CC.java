package dev.demeng.frost.util;

import dev.demeng.frost.Frost;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CC {

  @Setter private static boolean usingPlaceholderAPI = false;

  public static final String SB_BAR =
      ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------";
  public static final String MENU_BAR =
      ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------";
  public static final String DARK_MENU_BAR =
      ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------";
  public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH
      + "------------------------------------------------";

  public static final String TOP_SPLITTER = color(Frost.getInstance().getMenusConfig().getConfig()
      .getString("LEADERBOARDS-INVENTORY.TOP-SPLITTER"));
  public static final String BOTTOM_SPLITTER = color(
      Frost.getInstance().getMenusConfig().getConfig()
          .getString("LEADERBOARDS-INVENTORY.BOTTOM-SPLITTER"));

  public static void sendMessage(Player player, String message) {
    message = parse(player, message);
    player.sendMessage(color(message));
  }

  public static void sendTitle(Player player, String header, String footer) {
    Title title = new Title(parse(player, header), parse(player, footer), 1, 20, 0);
    player.sendTitle(title);
  }

  public static String color(String string) {
    return ChatColor.translateAlternateColorCodes('&', string);
  }

  public static List<String> color(List<String> lines) {
    List<String> toReturn = new ArrayList<>();
    for (String line : lines) {
      toReturn.add(ChatColor.translateAlternateColorCodes('&', line));
    }

    return toReturn;
  }

  public static String parse(Player player, String string) {
    if (usingPlaceholderAPI) {
      try {
        string = color(PlaceholderAPI.setPlaceholders(player, string));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      string = color(string);
    }

    return string;
  }

  public static List<String> parse(Player player, List<String> lines) {
    List<String> toReturn = new ArrayList<>();
    for (String line : lines) {
      toReturn.add(parse(player, color(line)));
    }

    return toReturn;
  }
}
