package dev.demeng.frost.user.ui.host.settings;

import dev.demeng.frost.game.kit.Kit;
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
public class EventStartButton extends Button {

  private final Kit kit;
  private final int teamSize, maxPlayers;
  private final EventSettingsMenu eventSettingsMenu;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = new ArrayList<>();
    for (String line : plugin.getMenusConfig().getConfig()
        .getStringList("EVENT-SETTINGS-MENU.START-TOURNAMENT.LORE")) {
      lore.add(CC.parse(player, line
              .replace("<kit>", kit.getName())
              .replace("<team>", String.valueOf(teamSize))
              .replace("<maxPlayers>", String.valueOf(maxPlayers))
          )
      );
    }

    return new ItemBuilder(Material.EMERALD)
        .name(CC.parse(player,
            plugin.getMenusConfig().getConfig().getString("EVENT-SETTINGS-MENU.START.NAME")))
        .lore(lore)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    player.closeInventory();

    if (eventSettingsMenu.getType().equalsIgnoreCase("tournament")) {
      if (plugin.getManagerHandler().getTournamentManager().getTournaments().size() == 0) {
        playSuccess(player);
        plugin.getManagerHandler().getTournamentManager()
            .createTournament(player, teamSize, maxPlayers, kit.getName());
      } else {
        playFail(player);
        player.sendMessage(CC.color("&cThere is an ongoing tournament currently."));
      }
    } else {
      if (plugin.getManagerHandler().getEventManager().getOngoingEvent() == null) {
        playSuccess(player);
        if (plugin.getManagerHandler().getEventManager().getByName(eventSettingsMenu.getEvent())
            .getName().equalsIgnoreCase("skywars")) {
          plugin.getManagerHandler().getEventManager().hostEvent(
              plugin.getManagerHandler().getEventManager().getByName(eventSettingsMenu.getEvent()),
              kit, plugin.getManagerHandler().getSpawnManager().getSkywarsLocations().size(),
              player);
        } else {
          plugin.getManagerHandler().getEventManager().hostEvent(
              plugin.getManagerHandler().getEventManager().getByName(eventSettingsMenu.getEvent()),
              kit, maxPlayers, player);
        }
      } else {
        playFail(player);
        player.sendMessage(CC.color("&cThere is an ongoing event currently."));
      }
    }
  }
}
