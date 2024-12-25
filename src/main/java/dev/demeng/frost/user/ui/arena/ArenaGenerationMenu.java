package dev.demeng.frost.user.ui.arena;

import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.user.ui.arena.buttons.ArenaGenerateButton;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ArenaGenerationMenu extends Menu {

  private final Arena arena;

  @Getter private final int[] clonableAmounts = {1, 2, 3, 4, 5, 10, 15};

  @Override
  public String getTitle(Player player) {
    return "Arena Copies Generation";
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    for (int curr : clonableAmounts) {
      buttons.put(1 + buttons.size(), new ArenaGenerateButton(arena, curr));
    }

    return buttons;
  }
}
