package dev.demeng.frost.user.ui.leaderboard.winstreak.style.minemen;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.ui.leaderboard.buttons.LeaderboardButton;
import dev.demeng.frost.user.ui.leaderboard.buttons.PlayerStatsButton;
import dev.demeng.frost.user.ui.leaderboard.buttons.WinstreakButton;
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

public class MMCWinstreaksMenu extends Menu {

  public MMCWinstreaksMenu() {
    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, plugin.getSettingsConfig().getConfig()
        .getString("SETTINGS.LEADERBOARDS.WINSTREAK.MENU-TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    buttons.put(4, new WinstreakGlobalButton(Material.SUGAR, "globalWinStreak"));

    AtomicInteger value = new AtomicInteger(19);
    plugin.getManagerHandler().getKitManager().getKits().stream()
        .sorted(Comparator.comparingInt(Kit::getRankedPos)).forEach(kit -> {
          buttons.put(value.getAndIncrement(), new WinstreakKitButton(kit));
          if (value.get() == 26) {
            value.set(28);
          }
          if (value.get() == 35) {
            value.set(37);
          }
        });

    buttons.put(57, new LeaderboardButton());
    buttons.put(58, new PlayerStatsButton());
    buttons.put(59, new WinstreakButton());

    return buttons;
  }

  @Override
  public int getSize() {
    return 9 * 7;
  }
}
