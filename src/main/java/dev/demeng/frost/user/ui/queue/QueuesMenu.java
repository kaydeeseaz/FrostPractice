package dev.demeng.frost.user.ui.queue;

import dev.demeng.frost.game.tournament.TournamentState;
import dev.demeng.frost.user.ui.queue.buttons.EventsButton;
import dev.demeng.frost.user.ui.queue.buttons.FFAButton;
import dev.demeng.frost.user.ui.queue.buttons.RankedButton;
import dev.demeng.frost.user.ui.queue.buttons.TournamentButton;
import dev.demeng.frost.user.ui.queue.buttons.UnrankedButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class QueuesMenu extends Menu {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(), "QUEUES");

  public QueuesMenu() {
    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, config.getString("TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    if (config.getBoolean("TYPES.UNRANKED.ENABLED")) {
      buttons.put(config.getInt("TYPES.UNRANKED.SLOT"), new UnrankedButton());
    }

    if (config.getBoolean("TYPES.RANKED.ENABLED")) {
      buttons.put(config.getInt("TYPES.RANKED.SLOT"), new RankedButton());
    }

    if (config.getBoolean("TYPES.FFA.ENABLED")) {
      buttons.put(config.getInt("TYPES.FFA.SLOT"), new FFAButton());
    }

    if (config.getBoolean("TYPES.EVENT.ENABLED")
        && plugin.getManagerHandler().getEventManager().getOngoingEvent() != null) {
      buttons.put(config.getInt("TYPES.EVENT.SLOT"), new EventsButton());
    }

    if (config.getBoolean("TYPES.TOURNAMENT.ENABLED")
        && plugin.getManagerHandler().getTournamentManager().getTournaments().size() >= 1
        && plugin.getManagerHandler().getTournamentManager().getTournament().getTournamentState()
        == TournamentState.WAITING) {
      buttons.put(config.getInt("TYPES.TOURNAMENT.SLOT"), new TournamentButton());
    }

    return buttons;
  }

  @Override
  public int getSize() {
    return config.getInt("SIZE") * 9;
  }
}
