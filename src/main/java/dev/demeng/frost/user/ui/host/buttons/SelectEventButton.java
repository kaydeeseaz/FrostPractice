package dev.demeng.frost.user.ui.host.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.user.ui.host.settings.EventSettingsMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class SelectEventButton extends Button {

  private final String name;
  private final Material material;
  private final int durability;
  private final List<String> lore;
  private final String command;
  private final String permission;

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(material).name(ChatColor.AQUA + ChatColor.BOLD.toString() + name)
        .amount(1).lore(Lists.newArrayList(lore)).durability(durability).build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    if (player.hasPermission(permission)) {
      player.closeInventory();

      if (plugin.getManagerHandler().getSpawnManager().getEventLocation(command.split(" ")[1])
          == null) {
        playFail(player);
        player.sendMessage(
            CC.color("&cThere is no event location set for the " + name + "&c event."));
      } else {
        playSuccess(player);
        new EventSettingsMenu("event", command.split(" ")[1]).openMenu(player);
      }
    } else {
      playFail(player);
      player.sendMessage(CC.color("&cYou don't have permissions to host this event!"));
    }
  }
}
