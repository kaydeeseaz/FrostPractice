package dev.demeng.frost.user.ui.arena;

import dev.demeng.frost.user.ui.arena.buttons.ArenaButton;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.pagination.PageButton;
import dev.demeng.frost.util.menu.pagination.PaginatedMenu;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArenaManagerMenu extends PaginatedMenu {

  @Override
  public String getPrePaginatedTitle(Player player) {
    return "Arena Management";
  }

  @Override
  public Map<Integer, Button> getAllPagesButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();
    plugin.getManagerHandler().getArenaManager().getArenas()
        .forEach((s, arena) -> buttons.put(buttons.size(), new ArenaButton(arena)));

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
  public int getSize() {
    return 9 * 5;
  }

  @Override
  public int getMaxItemsPerPage(Player player) {
    return 9 * 3;
  }
}
