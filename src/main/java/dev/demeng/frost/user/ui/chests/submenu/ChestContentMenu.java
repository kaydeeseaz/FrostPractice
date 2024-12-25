package dev.demeng.frost.user.ui.chests.submenu;

import dev.demeng.frost.managers.chest.Chest;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChestContentMenu extends Menu {

  private final Chest chest;

  public ChestContentMenu(Chest chest) {
    this.chest = chest;
  }

  @Override
  public String getTitle(Player player) {
    return "Chest #" + chest.getNumber();
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    if (chest.getItems() == null) {
      return buttons;
    }
    for (ItemStack item : chest.getItems()) {
      buttons.put(buttons.size(), new ChestContentButton(item));
    }

    return buttons;
  }

  private class ChestContentButton extends Button {

    private final ItemStack item;

    public ChestContentButton(ItemStack item) {
      this.item = item;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
      return item;
    }
  }
}
