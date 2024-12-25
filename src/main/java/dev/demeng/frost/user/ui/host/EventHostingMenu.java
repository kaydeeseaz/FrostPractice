package dev.demeng.frost.user.ui.host;

import dev.demeng.frost.user.ui.ConfigurableMenuUtil;
import dev.demeng.frost.user.ui.host.buttons.SelectEventButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class EventHostingMenu extends Menu {

  private final ConfigurableMenuUtil menuUtil = new ConfigurableMenuUtil(this.plugin);

  public EventHostingMenu() {
    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player,
        plugin.getMenusConfig().getConfig().getString("EVENTS-INVENTORY.TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    if (menuUtil.isHostable("SUMO")) {
      buttons.put(menuUtil.getEventSlot("SUMO"),
          new SelectEventButton(menuUtil.getEventName("SUMO"), menuUtil.getEventIcon("SUMO"), 0,
              Arrays.asList(menuUtil.getEventLore("SUMO")), "host sumo", "frost.event.sumo")
      );
    }

    if (menuUtil.isHostable("TNT_TAG")) {
      buttons.put(menuUtil.getEventSlot("TNT_TAG"),
          new SelectEventButton(menuUtil.getEventName("TNT_TAG"), menuUtil.getEventIcon("TNT_TAG"),
              0,
              Arrays.asList(menuUtil.getEventLore("TNT_TAG")), "host tnttag", "frost.event.tnttag")
      );
    }

    if (menuUtil.isHostable("OITC")) {
      buttons.put(menuUtil.getEventSlot("OITC"),
          new SelectEventButton(menuUtil.getEventName("OITC"), menuUtil.getEventIcon("OITC"), 0,
              Arrays.asList(menuUtil.getEventLore("OITC")), "host oitc", "frost.event.oitc")
      );
    }

    if (menuUtil.isHostable("BRACKETS")) {
      buttons.put(menuUtil.getEventSlot("BRACKETS"),
          new SelectEventButton(menuUtil.getEventName("BRACKETS"),
              menuUtil.getEventIcon("BRACKETS"), 0,
              Arrays.asList(menuUtil.getEventLore("BRACKETS")), "host brackets",
              "frost.event.brackets")
      );
    }

    if (menuUtil.isHostable("LMS")) {
      buttons.put(menuUtil.getEventSlot("LMS"),
          new SelectEventButton(menuUtil.getEventName("LMS"), menuUtil.getEventIcon("LMS"), 0,
              Arrays.asList(menuUtil.getEventLore("LMS")), "host lms", "frost.event.lms")
      );
    }

    if (menuUtil.isHostable("KNOCKOUT")) {
      buttons.put(menuUtil.getEventSlot("KNOCKOUT"),
          new SelectEventButton(menuUtil.getEventName("KNOCKOUT"),
              menuUtil.getEventIcon("KNOCKOUT"), 0,
              Arrays.asList(menuUtil.getEventLore("KNOCKOUT")), "host knockout",
              "frost.event.knockout")
      );
    }

    if (menuUtil.isHostable("SKYWARS")) {
      buttons.put(menuUtil.getEventSlot("SKYWARS"),
          new SelectEventButton(menuUtil.getEventName("SKYWARS"), menuUtil.getEventIcon("SKYWARS"),
              0,
              Arrays.asList(menuUtil.getEventLore("SKYWARS")), "host skywars",
              "frost.event.skywars")
      );
    }

    if (menuUtil.isHostable("PARKOUR")) {
      buttons.put(menuUtil.getEventSlot("PARKOUR"),
          new SelectEventButton(menuUtil.getEventName("PARKOUR"), menuUtil.getEventIcon("PARKOUR"),
              0,
              Arrays.asList(menuUtil.getEventLore("PARKOUR")), "host parkour",
              "frost.event.parkour")
      );
    }

    if (menuUtil.isHostable("GULAG")) {
      buttons.put(menuUtil.getEventSlot("GULAG"),
          new SelectEventButton(menuUtil.getEventName("GULAG"), menuUtil.getEventIcon("GULAG"), 0,
              Arrays.asList(menuUtil.getEventLore("GULAG")), "host gulag", "frost.event.gulag")
      );
    }

    if (menuUtil.isHostable("CORNERS")) {
      buttons.put(menuUtil.getEventSlot("CORNERS"),
          new SelectEventButton(menuUtil.getEventName("CORNERS"), menuUtil.getEventIcon("CORNERS"),
              0,
              Arrays.asList(menuUtil.getEventLore("CORNERS")), "host 4corners",
              "frost.event.corners")
      );
    }

    if (menuUtil.isHostable("THIMBLE")) {
      buttons.put(menuUtil.getEventSlot("THIMBLE"),
          new SelectEventButton(menuUtil.getEventName("THIMBLE"), menuUtil.getEventIcon("THIMBLE"),
              0,
              Arrays.asList(menuUtil.getEventLore("THIMBLE")), "host thimble",
              "frost.event.thimble")
      );
    }

    if (menuUtil.isHostable("DROPPER")) {
      buttons.put(menuUtil.getEventSlot("DROPPER"),
          new SelectEventButton(menuUtil.getEventName("DROPPER"), menuUtil.getEventIcon("DROPPER"),
              0,
              Arrays.asList(menuUtil.getEventLore("DROPPER")), "host dropper",
              "frost.event.dropper")
      );
    }

    if (menuUtil.isHostable("STOPLIGHT")) {
      buttons.put(menuUtil.getEventSlot("STOPLIGHT"),
          new SelectEventButton(menuUtil.getEventName("STOPLIGHT"),
              menuUtil.getEventIcon("STOPLIGHT"), 0,
              Arrays.asList(menuUtil.getEventLore("STOPLIGHT")), "host stoplight",
              "frost.event.stoplight")
      );
    }

    if (menuUtil.isHostable("SPLEEF")) {
      buttons.put(menuUtil.getEventSlot("SPLEEF"),
          new SelectEventButton(menuUtil.getEventName("SPLEEF"), menuUtil.getEventIcon("SPLEEF"), 0,
              Arrays.asList(menuUtil.getEventLore("SPLEEF")), "host spleef", "frost.event.spleef")
      );
    }

    return buttons;
  }

  @Override
  public int getSize() {
    return plugin.getMenusConfig().getConfig().getInt("EVENTS-INVENTORY.SIZE") * 9;
  }
}
