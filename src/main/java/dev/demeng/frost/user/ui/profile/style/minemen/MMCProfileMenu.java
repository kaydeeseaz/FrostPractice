package dev.demeng.frost.user.ui.profile.style.minemen;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.ui.profile.buttons.PracticeProfileButton;
import dev.demeng.frost.user.ui.profile.buttons.ProfileKitButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.entity.Player;

public class MMCProfileMenu extends Menu {

  private final Player target;

  public MMCProfileMenu(Player target) {
    this.target = target;

    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, plugin.getMenusConfig().getConfig().getString("PLAYER-PROFILE.TITLE")
        .replace("<player>", target.getName()));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    buttons.put(4, new PracticeProfileButton(target));

    AtomicInteger value = new AtomicInteger(19);
    plugin.getManagerHandler().getKitManager().getKits().stream().filter(Kit::isRanked)
        .sorted(Comparator.comparingInt(Kit::getRankedPos)).forEach(kit -> {
          buttons.put(value.getAndIncrement(), new ProfileKitButton(target, kit));
          if (value.get() == 26) {
            value.set(28);
          }
          if (value.get() == 35) {
            value.set(37);
          }
        });

    return buttons;
  }

  @Override
  public int getSize() {
    return plugin.getMenusConfig().getConfig().getInt("PLAYER-PROFILE.SIZE") * 9;
  }
}
