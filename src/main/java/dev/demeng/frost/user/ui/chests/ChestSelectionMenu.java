package dev.demeng.frost.user.ui.chests;

import dev.demeng.frost.managers.chest.Chest;
import dev.demeng.frost.user.ui.chests.buttons.OpenChestButton;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.pagination.PageButton;
import dev.demeng.frost.util.menu.pagination.PaginatedMenu;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ChestSelectionMenu extends PaginatedMenu {

  @Override
  public String getPrePaginatedTitle(Player player) {
    return "SW Chest Manager";
  }

  @Override
  public Map<Integer, Button> getAllPagesButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    for (Chest chest : plugin.getManagerHandler().getChestManager().getChests()) {
      buttons.put(buttons.size(), new OpenChestButton(chest));
    }

    return buttons;
  }

  @Override
  public Map<Integer, Button> getGlobalButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    buttons.put(0, new PageButton(-1, this));
    buttons.put(8, new PageButton(1, this));

    bottomTopButtons(false, buttons,
        new ItemBuilder(Material.STAINED_GLASS_PANE).name(" ").durability(15).build());

    return buttons;
  }

  @Override
  public int getMaxItemsPerPage(Player player) {
    return 9;
  }

  @Override
  public int getSize() {
    return 9 * 3;
  }
}
