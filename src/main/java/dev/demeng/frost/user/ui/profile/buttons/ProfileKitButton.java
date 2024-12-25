package dev.demeng.frost.user.ui.profile.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class ProfileKitButton extends Button {

  private final Player target;
  private final Kit kit;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(target.getUniqueId());

    int wins = practicePlayerData.getWins(kit.getName());
    int losses = practicePlayerData.getLosses(kit.getName());

    for (String string : plugin.getMenusConfig().getConfig()
        .getStringList("PLAYER-PROFILE.KIT.LORE")) {
      lore.add(CC.parse(player, string)
          .replace("<elo>", String.valueOf(practicePlayerData.getElo(kit.getName())))
          .replace("<wins>", String.valueOf(wins))
          .replace("<losses>", String.valueOf(losses))
          .replace("<ratio>", String.valueOf((double) wins / losses))
      );
    }

    return new ItemBuilder(kit.getIcon().getType())
        .name(CC.parse(player,
                plugin.getMenusConfig().getConfig().getString("PLAYER-PROFILE.KIT.NAME"))
            .replace("<kit>", kit.getName()))
        .durability(kit.getIcon().getDurability())
        .hideFlags()
        .lore(lore)
        .build();
  }
}
