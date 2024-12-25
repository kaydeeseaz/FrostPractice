package dev.demeng.frost.user.ui.chests.buttons;

import dev.demeng.frost.managers.chest.Chest;
import dev.demeng.frost.user.ui.chests.submenu.ChestContentMenu;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class OpenChestButton extends Button {

  private final Chest chest;

  public OpenChestButton(Chest chest) {
    this.chest = chest;
  }

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.CHEST).name("&bOpen Chest #" + chest.getNumber()).build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    new ChestContentMenu(chest).openMenu(player);
  }
}
