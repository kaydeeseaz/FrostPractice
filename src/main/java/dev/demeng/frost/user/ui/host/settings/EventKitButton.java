package dev.demeng.frost.user.ui.host.settings;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class EventKitButton extends Button {

  private final List<Kit> kits;
  private final Kit selectedKit;
  private final EventSettingsMenu eventSettingsMenu;

  public EventKitButton(Kit selectedKit, EventSettingsMenu eventSettingsMenu) {
    this.kits = new ArrayList<>(plugin.getManagerHandler().getKitManager().getKits());
    if (eventSettingsMenu.getType().equalsIgnoreCase("event")) {
      if (!eventSettingsMenu.getEvent().equalsIgnoreCase("skywars")) {
        for (String buildableKit : plugin.getManagerHandler().getKitManager().getBuildKitNames()) {
          this.kits.remove(plugin.getManagerHandler().getKitManager().getKit(buildableKit));
        }
      }
    }

    this.selectedKit = selectedKit;
    this.eventSettingsMenu = eventSettingsMenu;
  }

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = new ArrayList<>();
    for (String line : plugin.getMenusConfig().getConfig()
        .getStringList("EVENT-SETTINGS-MENU.KIT-SELECTION.LORE")) {
      lore.add(CC.parse(player, line.replace("<kit>", selectedKit.getName())));
    }

    return new ItemBuilder(selectedKit.getIcon().clone())
        .name(CC.parse(player,
            plugin.getMenusConfig().getConfig().getString("EVENT-SETTINGS-MENU.KIT-SELECTION.COLOR")
                + selectedKit.getName()))
        .hideFlags()
        .lore(lore)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    int kitPosition = kits.indexOf(selectedKit);
    int maxPosition = kits.size() - 1;

    if (clickType.isLeftClick()) {
      if (kitPosition == maxPosition) {
        eventSettingsMenu.setKit(kits.get(0));
      } else {
        eventSettingsMenu.setKit(kits.get(kitPosition + 1));
      }
    } else if (clickType.isRightClick()) {
      if (kitPosition == 0) {
        eventSettingsMenu.setKit(kits.get(maxPosition));
      } else {
        eventSettingsMenu.setKit(kits.get(kitPosition - 1));
      }
    }
  }
}
