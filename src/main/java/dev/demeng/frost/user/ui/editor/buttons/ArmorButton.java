package dev.demeng.frost.user.ui.editor.buttons;

import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorButton extends Button {

  private final ItemStack itemStack;

  public ArmorButton(ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  @Override
  public ItemStack getButtonItem(Player player) {
    if (this.itemStack == null || this.itemStack.getType() == Material.AIR) {
      return new ItemStack(Material.AIR);
    }

    return new ItemBuilder(this.itemStack.clone()).name(
            ChatColor.AQUA + this.itemStack.getType().name())
        .lore(Arrays.asList("", "&aThis is automatically equipped.")).build();
  }
}
