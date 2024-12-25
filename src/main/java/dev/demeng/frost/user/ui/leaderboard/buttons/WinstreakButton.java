package dev.demeng.frost.user.ui.leaderboard.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class WinstreakButton extends Button {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "LEADERBOARDS-INVENTORY.WINSTREAK-OPEN-ITEM");

  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();

    for (String string : config.getStringList("LORE")) {
      lore.add(CC.parse(player, string));
    }

    return new ItemBuilder(
        Material.valueOf(config.getString("MATERIAL")))
        .name(CC.parse(player, config.getString("NAME")))
        .lore(lore)
        .durability(config.getInt("DATA"))
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    PlayerUtil.getStyle(player, null, "STREAK", this.plugin);
    playNeutral(player);
  }
}
