package dev.demeng.frost.user.ui.editor.buttons;

import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class LoadButton extends Button {

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.WOOL)
        .durability(4)
        .name(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Load default kit")
        .lore(Arrays.asList(
            "",
            ChatColor.GRAY + "Click this to load the default kit",
            ChatColor.GRAY + "into the kit editing menu."
        )).build();
  }

  @Override
  public void clicked(Player player, int i, ClickType clickType, int hb) {
    if (player.getItemOnCursor().getType() != null) {
      player.setItemOnCursor(null);
    }

    player.getInventory().setContents(
        plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
            .getSelectedLadder().getContents());
    player.updateInventory();
    Button.playNeutral(player);
  }
}
