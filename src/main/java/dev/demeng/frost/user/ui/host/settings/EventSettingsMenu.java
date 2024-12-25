package dev.demeng.frost.user.ui.host.settings;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class EventSettingsMenu extends Menu {

  private final ConfigCursor cursor = new ConfigCursor(plugin.getMenusConfig(),
      "EVENT-SETTINGS-MENU");

  @Getter private final String type;
  @Getter private final String event;
  @Getter @Setter private Kit kit = plugin.getManagerHandler().getKitManager().getKit();
  @Setter private int teamSize = 1, maxPlayers = 10;

  public EventSettingsMenu(String type, String event) {
    this.type = type;
    this.event = event;
    setUpdateAfterClick(true);
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, cursor.getString("TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    if (event.equalsIgnoreCase("brackets") || event.equalsIgnoreCase("skywars")
        || event.equalsIgnoreCase("lms") || event.equalsIgnoreCase("knockout")
        || type.equalsIgnoreCase("tournament")) {
      buttons.put(this.cursor.getInt("KIT-SELECTION.SLOT"), new EventKitButton(kit, this));
    }
    if (type.equalsIgnoreCase("tournament")) {
      buttons.put(this.cursor.getInt("TEAM-SIZE-SELECTION.SLOT"),
          new EventTeamSizeButton(teamSize, this));
    }
    if (!event.equalsIgnoreCase("skywars") || type.equalsIgnoreCase("tournament")) {
      buttons.put(this.cursor.getInt("MAX-PLAYERS-SELECTION.SLOT"),
          new EventMaxPlayersButton(maxPlayers, this));
    }

    buttons.put(this.cursor.getInt("START.SLOT"),
        new EventStartButton(kit, teamSize, maxPlayers, this));

    return buttons;
  }

  @Override
  public int getSize() {
    return cursor.getInt("SIZE") * 9;
  }
}
