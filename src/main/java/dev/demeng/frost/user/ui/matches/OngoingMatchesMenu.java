package dev.demeng.frost.user.ui.matches;

import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.managers.InventoryManager;
import dev.demeng.frost.user.ui.matches.buttons.OngoingEventButton;
import dev.demeng.frost.user.ui.matches.buttons.OngoingMatchButton;
import dev.demeng.frost.user.ui.matches.buttons.RefreshButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.pagination.PageButton;
import dev.demeng.frost.util.menu.pagination.PaginatedMenu;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class OngoingMatchesMenu extends PaginatedMenu {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "ONGOING-MATCHES-INVENTORY");

  public OngoingMatchesMenu() {
    setUpdateAfterClick(true);
  }

  @Override
  public Map<Integer, Button> getAllPagesButtons(Player player) {
    Map<Integer, Button> buttons = new ConcurrentHashMap<>();

    int slot = 0;

    for (Map.Entry<UUID, Match> entry : plugin.getManagerHandler().getMatchManager().getMatches()
        .entrySet()) {
      Match match = entry.getValue();
      if (!match.isParty() || !match.isPartyMatch() || !match.isFFA()) {
        buttons.put(slot, new OngoingMatchButton(match));
        slot++;
      }
    }

    return buttons;
  }

  @Override
  public Map<Integer, Button> getGlobalButtons(Player player) {
    Map<Integer, Button> buttons = new ConcurrentHashMap<>();

    buttons.put(0, new PageButton(-1, this));

    if (plugin.getManagerHandler().getEventManager().getOngoingEvent() != null) {
      buttons.put(5, new RefreshButton(this));
      buttons.put(3,
          new OngoingEventButton(plugin.getManagerHandler().getEventManager().getOngoingEvent()));
    } else {
      buttons.put(4, new RefreshButton(this));
    }

    buttons.put(8, new PageButton(1, this));

    bottomTopButtons(false, buttons, InventoryManager.PLACEHOLDER_ITEM);

    return buttons;
  }

  @Override
  public int getSize() {
    return 9 * (this.config.getInt("SIZE") + 1);
  }

  @Override
  public int getMaxItemsPerPage(Player player) {
    return 9 * this.config.getInt("SIZE");
  }

  @Override
  public String getPrePaginatedTitle(Player player) {
    return CC.parse(player, this.config.getString("TITLE").replace("<amount>",
        String.valueOf(plugin.getManagerHandler().getMatchManager().getMatches().values().size())));
  }
}
