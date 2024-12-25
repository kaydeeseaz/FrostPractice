package dev.demeng.frost.user.player.match.menu;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.match.MatchLocatedData;
import dev.demeng.frost.user.player.match.menu.button.MatchHistoryButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.pagination.PaginatedMenu;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class MatchHistoryMenu extends PaginatedMenu {

  private final UUID uuid;
  private final List<MatchLocatedData> matchLocatedData;

  @Override
  public String getTitle(Player player) {
    return CC.parse(player,
        Frost.getInstance().getMenusConfig().getConfig().getString("MATCH-HISTORY.TITLE")
            .replace("<player>", Bukkit.getOfflinePlayer(uuid).getName())
            .replace("<current>", String.valueOf(getPage()))
            .replace("<last>", String.valueOf(getPages(player)))
    );
  }

  @Override
  public String getPrePaginatedTitle(Player player) {
    return "null";
  }

  @Override
  public Map<Integer, Button> getGlobalButtons(Player player) {
    Map<Integer, Button> map = new HashMap<>();

    Button button = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15, "");
    for (int i = 1; i < 8; i++) {
      map.put(i, button);
    }

    return map;
  }

  @Override
  public Map<Integer, Button> getAllPagesButtons(Player player) {
    Map<Integer, Button> buttonMap = new HashMap<>();

    this.matchLocatedData.stream()
        .sorted(Comparator.comparing(MatchLocatedData::getDate).reversed()).forEach(matchData -> {
          buttonMap.put(buttonMap.size(), new MatchHistoryButton(matchData));
        });

    return buttonMap;
  }

  @Override
  public int getMaxItemsPerPage(Player player) {
    return 18;
  }
}
