package dev.demeng.frost.tablist.adapter;

import dev.demeng.frost.tablist.entry.TabEntry;
import java.util.List;
import org.bukkit.entity.Player;

public interface TabAdapter {

  /**
   * Get the tab header for a player.
   *
   * @param player the player
   * @return string
   */
  String getHeader(Player player);

  /**
   * Get the tab player for a player.
   *
   * @param player the player
   * @return string
   */
  String getFooter(Player player);

  /**
   * Get the tab lines for a player.
   *
   * @param player the player
   * @return map
   */
  List<TabEntry> getLines(Player player);
}
