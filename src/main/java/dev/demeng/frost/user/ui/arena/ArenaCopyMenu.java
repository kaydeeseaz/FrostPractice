package dev.demeng.frost.user.ui.arena;

import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.user.ui.arena.buttons.ArenaCopyButton;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ArenaCopyMenu extends Menu {

  private final Arena arena;

  @Override
  public String getTitle(Player player) {
    return "Arena Copies";
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    int i = 0;
    for (StandaloneArena arenaCopy : plugin.getManagerHandler().getArenaManager()
        .getArena(arena.getName()).getStandaloneArenas()) {
      buttons.put(buttons.size(), new ArenaCopyButton(i, arena, arenaCopy));
      i++;
    }

    return buttons;
  }
}
