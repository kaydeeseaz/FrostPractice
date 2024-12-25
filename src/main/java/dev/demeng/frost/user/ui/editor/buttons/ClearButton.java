package dev.demeng.frost.user.ui.editor.buttons;

import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ClearButton extends Button {

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.WOOL)
        .durability(1)
        .name(ChatColor.GOLD.toString() + ChatColor.BOLD + "Clear Inventory")
        .lore(Arrays.asList(
            "",
            ChatColor.GRAY + "This will clear your inventory",
            ChatColor.GRAY + "so you can start over."
        )).build();
  }

  @Override
  public void clicked(Player player, int i, ClickType clickType, int hb) {
    Button.playNeutral(player);
    player.getInventory().clear();
    player.updateInventory();
  }
}
