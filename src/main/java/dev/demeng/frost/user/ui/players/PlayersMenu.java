package dev.demeng.frost.user.ui.players;

import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.managers.InventoryManager;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.pagination.PageButton;
import dev.demeng.frost.util.menu.pagination.PaginatedMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PlayersMenu extends PaginatedMenu {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(), "PLAYERS-MENU");

  private Match match;
  private PracticeEvent<?> event;

  public PlayersMenu(PracticeEvent<?> event) {
    this.event = event;
  }

  public PlayersMenu(Match match) {
    this.match = match;
  }

  @Override
  public boolean isUpdateAfterClick() {
    return true;
  }

  @Override
  public String getPrePaginatedTitle(Player player) {
    int amount = (this.match == null ? this.event.getPlayers().size() : this.getPlayers());
    return CC.parse(player, this.config.getString("TITLE")
        .replace("<current>", (this.match == null ? "Event" : "Match"))
        .replace("<amount>", String.valueOf(amount)));
  }

  @Override
  public Map<Integer, Button> getAllPagesButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    if (this.match == null) {
      this.event.getBukkitPlayers()
          .forEach(ePlayer -> buttons.put(buttons.size(), new PlayersButton(ePlayer)));
    } else if (event == null) {
      this.match.getTeams().forEach(team -> team.alivePlayers()
          .forEach(mPlayer -> buttons.put(buttons.size(), new PlayersButton(mPlayer))));
    }

    return buttons;
  }

  @Override
  public Map<Integer, Button> getGlobalButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    buttons.put(0, new PageButton(-1, this));
    buttons.put(8, new PageButton(1, this));

    bottomTopButtons(false, buttons, InventoryManager.PLACEHOLDER_ITEM);

    return buttons;
  }

  @Override
  public int getSize() {
    return 9 * 4;
  }

  @Override
  public int getMaxItemsPerPage(Player player) {
    return 9 * 2;
  }

  private int getPlayers() {
    int i = 0;
    for (MatchTeam team : this.match.getTeams()) {
      i += team.getAlivePlayers().size();
    }

    return i;
  }

  @AllArgsConstructor
  private class PlayersButton extends Button {

    private Player player;

    @Override
    public ItemStack getButtonItem(Player player) {
      List<String> lore = new ArrayList<>();
      for (String string : config.getStringList("ITEM-LORE")) {
        lore.add(string.replace("<name>", this.player.getName()));
      }

      return new ItemBuilder(Material.SKULL_ITEM)
          .name(config.getString("ITEM-NAME").replace("<name>", this.player.getName()))
          .durability(3)
          .owner(this.player.getName())
          .lore(lore)
          .hideFlags()
          .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
      player.teleport(this.player);
    }
  }
}
