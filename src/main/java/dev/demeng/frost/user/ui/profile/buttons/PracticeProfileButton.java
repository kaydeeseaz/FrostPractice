package dev.demeng.frost.user.ui.profile.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class PracticeProfileButton extends Button {

  private final Player target;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(target.getUniqueId());

    for (String string : plugin.getMenusConfig().getConfig()
        .getStringList("PLAYER-PROFILE.PLAYER.LORE")) {
      lore.add(CC.parse(player, string)
          .replace("<globalElo>", String.valueOf(practicePlayerData.getGlobalElo()))
          .replace("<premiumElo>", String.valueOf(practicePlayerData.getPremiumElo()))
          .replace("<premiumMatches>", String.valueOf(practicePlayerData.getPremiumMatches()))
      );
    }

    return new ItemBuilder(Material.SKULL_ITEM)
        .name(CC.parse(player,
                plugin.getMenusConfig().getConfig().getString("PLAYER-PROFILE.PLAYER.NAME"))
            .replace("<player>", target.getName()))
        .owner(target.getName())
        .durability(3)
        .lore(lore)
        .build();
  }
}
