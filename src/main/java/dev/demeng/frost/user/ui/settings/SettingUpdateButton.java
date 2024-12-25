package dev.demeng.frost.user.ui.settings;

import static dev.demeng.frost.util.CC.color;
import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.user.player.PlayerSettings;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.settings.death.DeathEffectsMenu;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class SettingUpdateButton extends Button {

  private final String name;
  private final Material material;
  private final int durability;
  private final List<String> lore;
  private final String command;
  private final String type;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lines = new ArrayList<>(lore);
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    PlayerSettings settings = practicePlayerData.getPlayerSettings();

    ConfigCursor menu = new ConfigCursor(plugin.getMenusConfig(), "SETTINGS-INVENTORY");

    String enabled = color(menu.getString("ENABLED"));
    String disabled = color(menu.getString("DISABLED"));
    String unselected = color(menu.getString("UNSELECTED"));

    if (type.equalsIgnoreCase("duelRequests")) {
      lines.add((settings.isDuelRequests() ? enabled : unselected) + menu.getString(
          "SETTINGS.DUEL_REQUESTS.O-ENABLED"));
      lines.add((!settings.isDuelRequests() ? disabled : unselected) + menu.getString(
          "SETTINGS.DUEL_REQUESTS.O-DISABLED"));
    } else if (type.equalsIgnoreCase("partyInvites")) {
      lines.add((settings.isPartyInvites() ? enabled : unselected) + menu.getString(
          "SETTINGS.PARTY_REQUESTS.O-ENABLED"));
      lines.add((!settings.isPartyInvites() ? disabled : unselected) + menu.getString(
          "SETTINGS.PARTY_REQUESTS.O-DISABLED"));
    } else if (type.equalsIgnoreCase("pingScoreboardToggled")) {
      lines.add((settings.isPingScoreboardToggled() ? enabled : unselected) + menu.getString(
          "SETTINGS.PING_ON_SCOREBOARD.O-ENABLED"));
      lines.add((!settings.isPingScoreboardToggled() ? disabled : unselected) + menu.getString(
          "SETTINGS.PING_ON_SCOREBOARD.O-DISABLED"));
    } else if (type.equalsIgnoreCase("scoreboardToggled")) {
      lines.add((settings.isScoreboardToggled() ? enabled : unselected) + menu.getString(
          "SETTINGS.TOGGLE_SCOREBOARD.O-ENABLED"));
      lines.add((!settings.isScoreboardToggled() ? disabled : unselected) + menu.getString(
          "SETTINGS.TOGGLE_SCOREBOARD.O-DISABLED"));
    } else if (type.equalsIgnoreCase("spectatorsAllowed")) {
      lines.add((settings.isSpectatorsAllowed() ? enabled : unselected) + menu.getString(
          "SETTINGS.ALLOW_SPECTATORS.O-ENABLED"));
      lines.add((!settings.isSpectatorsAllowed() ? disabled : unselected) + menu.getString(
          "SETTINGS.ALLOW_SPECTATORS.O-DISABLED"));
    } else if (type.equalsIgnoreCase("vanillaTab")) {
      lines.add((settings.isVanillaTab() ? enabled : unselected) + menu.getString(
          "SETTINGS.VANILLA_TAB.O-ENABLED"));
      lines.add((!settings.isVanillaTab() ? disabled : unselected) + menu.getString(
          "SETTINGS.VANILLA_TAB.O-DISABLED"));
    } else if (type.equalsIgnoreCase("startFlying")) {
      lines.add((settings.isStartFlying() ? enabled : unselected) + menu.getString(
          "SETTINGS.START_FLYING.O-ENABLED"));
      lines.add((!settings.isStartFlying() ? disabled : unselected) + menu.getString(
          "SETTINGS.START_FLYING.O-DISABLED"));
    } else if (type.equalsIgnoreCase("clearInventory")) {
      lines.add((settings.isClearInventory() ? enabled : unselected) + menu.getString(
          "SETTINGS.CLEAR_INVENTORY.O-ENABLED"));
      lines.add((!settings.isClearInventory() ? disabled : unselected) + menu.getString(
          "SETTINGS.CLEAR_INVENTORY.O-DISABLED"));
    } else if (type.equalsIgnoreCase("bodyAnimation")) {
      lines.add((settings.isBodyAnimation() ? enabled : unselected) + menu.getString(
          "SETTINGS.BODY_ANIMATION.O-ENABLED"));
      lines.add((!settings.isBodyAnimation() ? disabled : unselected) + menu.getString(
          "SETTINGS.BODY_ANIMATION.O-DISABLED"));
    }

    lines.add(
        color(plugin.getMenusConfig().getConfig().getString("SETTINGS-INVENTORY.BOTTOM-SPLITTER")));

    return new ItemBuilder(material).name(
            color(menu.getString("NAME").replace("<setting_name>", name))).amount(1).lore(lines)
        .durability(durability).hideFlags().build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    switch (type) {
      case "deathEffectsSettings":
        if (!player.hasPermission("frost.vip.death_effects")) {
          sendMessage(player,
              plugin.getMenusConfig().getConfig().getString("SETTINGS-INVENTORY.NO-PERMISSION"));
          playFail(player);
        } else {
          new DeathEffectsMenu().openMenu(player);
          playSuccess(player);
        }
        break;
      case "timeSetting":
        PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(player.getUniqueId());
        final List<PlayerSettings.PlayerTime> settings = Arrays.stream(
            PlayerSettings.PlayerTime.values()).collect(Collectors.toList());

        int currentValue = settings.indexOf(practicePlayerData.getPlayerSettings().getPlayerTime());
        if (currentValue >= 2) {
          currentValue = 0;
        } else {
          currentValue++;
        }

        playSuccess(player);
        practicePlayerData.getPlayerSettings().setPlayerTime(settings.get(currentValue));
        player.setPlayerTime(settings.get(currentValue).getTime(), false);
        break;
      default:
        player.performCommand(command);
        playSuccess(player);
        break;
    }
  }
}
