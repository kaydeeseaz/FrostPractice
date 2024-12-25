package dev.demeng.frost.user.ui.editor.buttons;

import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.editor.KitManagementMenu;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SaveButton extends Button {

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.WOOL)
        .durability(5)
        .name(ChatColor.GREEN.toString() + ChatColor.BOLD + "Save")
        .lore(Arrays.asList(
            "",
            ChatColor.GRAY + "Click this to save your kit."
        )).build();
  }

  @Override
  public void clicked(Player player, int i, ClickType clickType, int hb) {
    Button.playNeutral(player);

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getSelectedKit() != null) {
      practicePlayerData.getSelectedKit().setContents(player.getInventory().getContents());
    }

    new KitManagementMenu(practicePlayerData.getSelectedLadder()).openMenu(player);
  }
}
