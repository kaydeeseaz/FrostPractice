package dev.demeng.frost.user.ui.leaderboard.winstreak.style.silex;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.ui.leaderboard.buttons.LeaderboardButton;
import dev.demeng.frost.user.ui.leaderboard.buttons.PlayerStatsButton;
import dev.demeng.frost.user.ui.leaderboard.winstreak.buttons.WinstreakGlobalButton;
import dev.demeng.frost.user.ui.leaderboard.winstreak.buttons.WinstreakKitButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SilexWinstreaksMenu extends Menu {

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, plugin.getSettingsConfig().getConfig()
        .getString("SETTINGS.LEADERBOARDS.WINSTREAK.MENU-TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    buttons.put(10, new WinstreakGlobalButton(Material.SUGAR, "globalWinStreak"));
    buttons.put(28, new PlayerStatsButton());
    buttons.put(37, new LeaderboardButton());

    AtomicInteger value = new AtomicInteger(12);
    plugin.getManagerHandler().getKitManager().getKits().stream()
        .sorted(Comparator.comparingInt(Kit::getRankedPos)).forEach(kit -> {
          buttons.put(value.getAndIncrement(), new WinstreakKitButton(kit));
          if (value.get() == 17) {
            value.set(30);
          }
          if (value.get() == 35) {
            value.set(21);
          }
          if (value.get() == 26) {
            value.set(39);
          }
        });

    return buttons;
  }

  @Override
  public int getSize() {
    return 9 * 6;
  }
}
