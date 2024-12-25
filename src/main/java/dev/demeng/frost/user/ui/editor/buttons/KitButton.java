package dev.demeng.frost.user.ui.editor.buttons;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitButton extends Button {

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.NAME_TAG)
        .name("&b&lEditing: &f" + Frost.getInstance().getManagerHandler().getPlayerManager()
            .getPlayerData(player.getUniqueId()).getSelectedKit().getName())
        .build();
  }
}
