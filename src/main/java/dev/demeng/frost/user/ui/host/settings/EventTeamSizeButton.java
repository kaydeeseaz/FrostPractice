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
public class EventTeamSizeButton extends Button {

  private final int selectedTeamSize;
  private final EventSettingsMenu eventSettingsMenu;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = new ArrayList<>();
    for (String line : plugin.getMenusConfig().getConfig()
        .getStringList("EVENT-SETTINGS-MENU.TEAM-SIZE-SELECTION.LORE")) {
      lore.add(CC.parse(player, line.replace("<team>", String.valueOf(selectedTeamSize))));
    }

    return new ItemBuilder(Material.BOOK_AND_QUILL)
        .name(CC.parse(player, plugin.getMenusConfig().getConfig()
            .getString("EVENT-SETTINGS-MENU.TEAM-SIZE-SELECTION.COLOR") + selectedTeamSize + "v"
            + selectedTeamSize))
        .lore(lore)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    int teamSize = selectedTeamSize;
    int maxTeamSize;

    if (eventSettingsMenu.getType().equalsIgnoreCase("tournament")) {
      maxTeamSize = 5;
    } else {
      maxTeamSize = 3;
    }

    if (clickType.isLeftClick()) {
      if (teamSize == maxTeamSize) {
        eventSettingsMenu.setTeamSize(1);
      } else {
        eventSettingsMenu.setTeamSize(teamSize + 1);
      }
    } else if (clickType.isRightClick()) {
      if (teamSize == 1 || teamSize == 0) {
        eventSettingsMenu.setTeamSize(maxTeamSize);
      } else {
        eventSettingsMenu.setTeamSize(teamSize - 1);
      }
    }
  }
}
