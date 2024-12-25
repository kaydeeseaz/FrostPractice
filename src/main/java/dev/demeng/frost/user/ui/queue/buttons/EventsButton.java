package dev.demeng.frost.user.ui.queue.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class EventsButton extends Button {

  private final PracticeEvent<?> event = plugin.getManagerHandler().getEventManager()
      .getOngoingEvent();

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "QUEUES.TYPES.EVENT");
  private final ConfigCursor eventConfig = new ConfigCursor(plugin.getMenusConfig(),
      "EVENTS-INVENTORY.EVENTS");

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();

    for (String text : config.getStringList("LORE")) {
      lore.add(text
          .replace("<event_name>", event.getName())
          .replace("<event_max_players>", String.valueOf(event.getLimit()))
          .replace("<event_players>", String.valueOf(event.getPlayers().size()))
      );
    }

    if (config.getString("ICON").equals("<current_event>")) {
      return new ItemBuilder(
          Material.valueOf(getCurrentItem()))
          .lore(CC.color(lore))
          .amount(1)
          .name(CC.parse(player, config.getString("NAME").replace("<event_name>", event.getName())))
          .hideFlags()
          .durability(config.getInt("DATA"))
          .build();
    } else {
      return new ItemBuilder(
          Material.valueOf(config.getString("ICON")))
          .lore(CC.color(lore))
          .amount(1)
          .name(CC.parse(player, config.getString("NAME").replace("<event_name>", event.getName())))
          .hideFlags()
          .durability(config.getInt("DATA"))
          .build();
    }
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    if (event == null) {
      player.sendMessage(CC.color("&cThere is no event going on at this moment!"));
      return;
    }

    player.performCommand("join_event " + event.getName().toLowerCase());
  }

  private String getCurrentItem() {
    switch (event.getName()) {
      case "Brackets":
        return eventConfig.getString("BRACKETS.ICON");
      case "4Corners":
        return eventConfig.getString("CORNERS.ICON");
      case "Gulag":
        return eventConfig.getString("GULAG.ICON");
      case "LMS":
        return eventConfig.getString("LMS.ICON");
      case "OITC":
        return eventConfig.getString("OITC.ICON");
      case "Sumo":
        return eventConfig.getString("SUMO.ICON");
      case "TNTTag":
        return eventConfig.getString("TNT_TAG.ICON");
      case "Parkour":
        return eventConfig.getString("PARKOUR.ICON");
      case "Spleef":
        return this.eventConfig.getString("SPLEEF.ICON");
      case "SkyWars":
        return eventConfig.getString("SKYWARS.ICON");
      case "Thimble":
        return eventConfig.getString("THIMBLE.ICON");
      case "Dropper":
        return eventConfig.getString("DROPPER.ICON");
      case "Knockout":
        return eventConfig.getString("KNOCKOUT.ICON");
      case "StopLight":
        return this.eventConfig.getString("STOPLIGHT.ICON");
    }

    return null;
  }
}
