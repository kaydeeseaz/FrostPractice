package dev.demeng.frost.user.ui.host.settings;

import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class EventMaxPlayersButton extends Button {

  private final int maxPlayers;
  private final EventSettingsMenu eventSettingsMenu;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = new ArrayList<>();
    for (String line : plugin.getMenusConfig().getConfig()
        .getStringList("EVENT-SETTINGS-MENU.MAX-PLAYERS-SELECTION.LORE")) {
      lore.add(CC.parse(player, line.replace("<maxPlayers>", String.valueOf(maxPlayers))));
    }

    return new ItemBuilder(Material.PAPER)
        .name(CC.parse(player, plugin.getMenusConfig().getConfig()
            .getString("EVENT-SETTINGS-MENU.MAX-PLAYERS-SELECTION.COLOR") + maxPlayers))
        .lore(lore)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    int maxPlayers = this.maxPlayers;
    int playersLimit;

    if (eventSettingsMenu.getType().equalsIgnoreCase("tournament")) {
      playersLimit = 200;
    } else {
      playersLimit = 50;
    }

    switch (clickType) {
      case LEFT: {
        if (maxPlayers >= playersLimit) {
          eventSettingsMenu.setMaxPlayers(10);
        } else {
          eventSettingsMenu.setMaxPlayers(maxPlayers + 10);
        }
        break;
      }
      case RIGHT: {
        if (maxPlayers <= 10) {
          eventSettingsMenu.setMaxPlayers(playersLimit);
        } else {
          eventSettingsMenu.setMaxPlayers(maxPlayers - 10);
        }
        break;
      }
    }
  }
}
