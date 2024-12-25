package dev.demeng.frost.user.ui.leaderboard.buttons;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.managers.leaderboard.Leaderboard;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.RatingUtil;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class KitStatsButton extends Button {

  private final Kit kit;

  @Override
  public ItemStack getButtonItem(Player player) {

    List<String> lore = new ArrayList<>();
    AtomicInteger lineNum = new AtomicInteger();

    lore.add(CC.TOP_SPLITTER);

    List<Leaderboard> leaderboards = new ArrayList<>(
        plugin.getManagerHandler().getLeaderboardManager().getSortedKitLeaderboards(kit, "elo"));
    for (Leaderboard leaderboard : leaderboards) {
      lineNum.getAndIncrement();
      lore.add(
          plugin.getMenusConfig().getConfig().getString("LEADERBOARDS-INVENTORY.KITS-STATS.LINE")
              .replace("<position>", String.valueOf(lineNum))
              .replace("<player>", leaderboard.getPlayerName())
              .replace("<elo>", String.valueOf(leaderboard.getPlayerElo()))
              .replace("<rating>", RatingUtil.getRankByElo(leaderboard.getPlayerElo()).getName())
      );
    }

    lore.add(CC.BOTTOM_SPLITTER);

    return new ItemBuilder(kit.getIcon().getType()).name(CC.parse(player,
                plugin.getMenusConfig().getConfig().getString("LEADERBOARDS-INVENTORY.KITS-STATS.TITLE"))
            .replace("<kit>", kit.getName())).lore(lore).durability(kit.getIcon().getDurability())
        .hideFlags().build();
  }
}
