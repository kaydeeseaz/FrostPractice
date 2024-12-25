package dev.demeng.frost.user.ui.leaderboard.style.minemen;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.ui.leaderboard.buttons.KitStatsButton;
import dev.demeng.frost.user.ui.leaderboard.buttons.LeaderboardButton;
import dev.demeng.frost.user.ui.leaderboard.buttons.LeaderboardGlobalButton;
import dev.demeng.frost.user.ui.leaderboard.buttons.PlayerStatsButton;
import dev.demeng.frost.user.ui.leaderboard.buttons.WinstreakButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MMCLeaderboardsMenu extends Menu {

  public MMCLeaderboardsMenu() {
    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player,
        plugin.getSettingsConfig().getConfig().getString("SETTINGS.LEADERBOARDS.MENU-TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    if (plugin.getSettingsConfig().getConfig().getBoolean("SETTINGS.GENERAL.PREMIUM-ENABLED")) {
      buttons.put(3, new LeaderboardGlobalButton(Material.NETHER_STAR, "globalElo", "GLOBAL"));
      buttons.put(5, new LeaderboardGlobalButton(Material.DIAMOND, "premiumElo", "PREMIUM"));
    } else {
      buttons.put(4, new LeaderboardGlobalButton(Material.NETHER_STAR, "globalElo", "GLOBAL"));
    }

    AtomicInteger value = new AtomicInteger(10);
    plugin.getManagerHandler().getKitManager().getKits().stream().filter(Kit::isRanked)
        .sorted(Comparator.comparingInt(Kit::getRankedPos)).forEach(kit -> {
              buttons.put(value.getAndIncrement(), new KitStatsButton(kit));
              if (value.get() == 17) {
                value.set(19);
              }
              if (value.get() == 26) {
                value.set(28);
              }
              if (value.get() == 35) {
                value.set(37);
              }
            }
        );

    buttons.put(48, new LeaderboardButton());
    buttons.put(49, new PlayerStatsButton());
    buttons.put(50, new WinstreakButton());

    return buttons;
  }

  @Override
  public int getSize() {
    return 9 * 6;
  }
}
