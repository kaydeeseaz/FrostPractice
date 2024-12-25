package dev.demeng.frost.user.ui.editor.buttons;

import dev.demeng.frost.util.menu.buttons.DisplayButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RefillableItemButton extends DisplayButton {

  public RefillableItemButton(ItemStack itemStack) {
    super(itemStack, false);
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbar) {
    Inventory inventory = player.getOpenInventory().getTopInventory();
    ItemStack itemStack = inventory.getItem(slot);

    inventory.setItem(slot, itemStack);

    player.setItemOnCursor(itemStack);
    player.updateInventory();
  }
}
