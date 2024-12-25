package dev.demeng.frost.user.ui.leaderboard.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class PlayerStatsButton extends Button {

  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();

    for (String string : plugin.getMenusConfig().getConfig()
        .getStringList("LEADERBOARDS-INVENTORY.PLAYER-STATS.LORE")) {
      lore.add(CC.parse(player, string));
    }

    return new ItemBuilder(Material.SKULL_ITEM).name(CC.parse(player,
                plugin.getMenusConfig().getConfig().getString("LEADERBOARDS-INVENTORY.PLAYER-STATS.TITLE"))
            .replace("<player>", player.getName())).owner(player.getName()).lore(lore).durability(3)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    playNeutral(player);
    PlayerUtil.getStyle(player, player, "STATS", this.plugin);
  }
}
