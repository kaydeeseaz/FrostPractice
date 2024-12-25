package dev.demeng.frost.user.ui.profile.style.minehq;

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
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class MHQProfileMenu extends Menu {

  private final Player target;

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, plugin.getMenusConfig().getConfig().getString("PLAYER-PROFILE.TITLE")
        .replace("<player>", target.getName()));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    buttons.put(13, new PracticeProfileButton(target));

    AtomicInteger value = new AtomicInteger(28);
    plugin.getManagerHandler().getKitManager().getKits().stream().filter(Kit::isRanked)
        .sorted(Comparator.comparingInt(Kit::getRankedPos)).forEach(kit -> {
          buttons.put(value.getAndIncrement(), new ProfileKitButton(target, kit));
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
