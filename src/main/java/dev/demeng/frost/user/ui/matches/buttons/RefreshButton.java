package dev.demeng.frost.user.ui.matches.buttons;

import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class RefreshButton extends Button {

  private Menu menu;

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.CARPET)
        .name("&bRefresh")
        .lore(Arrays.asList(" ", "&7Click here to update the fights"))
        .durability(5)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    playNeutral(player);
    this.menu.updateInventory(player);
  }
}
