package dev.demeng.frost.user.ui.editor.buttons;

import dev.demeng.frost.user.player.PlayerState;
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

public class CancelButton extends Button {

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.WOOL)
        .durability(14)
        .name(ChatColor.RED.toString() + ChatColor.BOLD + "Cancel")
        .lore(Arrays.asList(
            "",
            ChatColor.GRAY + "Click this to abort editing your kit,",
            ChatColor.GRAY + "and return to the kit menu."
        )).build();
  }

  @Override
  public void clicked(Player player, int i, ClickType clickType, int hb) {
    Button.playNeutral(player);

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    practicePlayerData.setPlayerState(PlayerState.SPAWN);
    practicePlayerData.setActive(false);

    new KitManagementMenu(practicePlayerData.getSelectedLadder()).openMenu(player);
  }
}
