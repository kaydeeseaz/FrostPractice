package dev.demeng.frost.user.ui.matches.buttons;

import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class OngoingEventButton extends Button {

  private final ConfigCursor menuConfig = new ConfigCursor(plugin.getMenusConfig(),
      "ONGOING-MATCHES-INVENTORY.EVENT-ITEM");
  private final ConfigCursor eventConfig = new ConfigCursor(plugin.getMenusConfig(),
      "EVENTS-INVENTORY.EVENTS");

  private PracticeEvent<?> event;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = new ArrayList<>();

    for (String string : this.menuConfig.getStringList("LORE")) {
      lore.add(string
          .replace("<host>", this.event.getHost().getName())
          .replace("<playing>", String.valueOf(this.event.getPlayers().size()))
          .replace("<limit>", String.valueOf(this.event.getLimit()))
          .replace("<eventName>", this.event.getName()));
    }

    return new ItemBuilder(Material.valueOf(this.getCurrentItem()))
        .lore(CC.color(lore))
        .amount(1)
        .name(this.menuConfig.getString("NAME").replace("<eventName>", this.event.getName()))
        .hideFlags()
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    plugin.getManagerHandler().getEventManager().addSpectator(player,
        plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
        this.event);
  }

  private String getCurrentItem() {
    switch (this.event.getName()) {
      case "Brackets":
        return this.eventConfig.getString("BRACKETS.ICON");
      case "4Corners":
        return this.eventConfig.getString("CORNERS.ICON");
      case "Gulag":
        return this.eventConfig.getString("GULAG.ICON");
      case "LMS":
        return this.eventConfig.getString("LMS.ICON");
      case "OITC":
        return this.eventConfig.getString("OITC.ICON");
      case "Sumo":
        return this.eventConfig.getString("SUMO.ICON");
      case "TNTTag":
        return this.eventConfig.getString("TNT_TAG.ICON");
      case "Parkour":
        return this.eventConfig.getString("PARKOUR.ICON");
      case "Spleef":
        return this.eventConfig.getString("SPLEEF.ICON");
      case "Thimble":
        return this.eventConfig.getString("THIMBLE.ICON");
      case "Dropper":
        return this.eventConfig.getString("DROPPER.ICON");
      case "Knockout":
        return this.eventConfig.getString("KNOCKOUT.ICON");
      case "StopLight":
        return this.eventConfig.getString("STOPLIGHT.ICON");
    }
    return null;
  }
}
