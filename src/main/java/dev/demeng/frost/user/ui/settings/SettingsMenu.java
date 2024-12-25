package dev.demeng.frost.user.ui.settings;

import dev.demeng.frost.user.ui.ConfigurableMenuUtil;
import dev.demeng.frost.user.ui.settings.matchmaking.PingRangeButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class SettingsMenu extends Menu {

  private final ConfigurableMenuUtil menuUtil = new ConfigurableMenuUtil(this.plugin);

  public SettingsMenu() {
    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }

    setUpdateAfterClick(true);
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player,
        plugin.getMenusConfig().getConfig().getString("SETTINGS-INVENTORY.TITLE"));
  }

  @Override
  public int getSize() {
    return plugin.getMenusConfig().getConfig().getInt("SETTINGS-INVENTORY.SIZE") * 9;
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    if (menuUtil.isShowingSetting("DUEL_REQUESTS")) {
      buttons.put(menuUtil.getSettingSlot("DUEL_REQUESTS"),
          new SettingUpdateButton(menuUtil.getSettingName("DUEL_REQUESTS"),
              menuUtil.getSettingIcon("DUEL_REQUESTS"), 0,
              Arrays.asList(menuUtil.getSettingLore("DUEL_REQUESTS")), "toggleduelrequests",
              "duelRequests")
      );
    }

    if (menuUtil.isShowingSetting("PARTY_REQUESTS")) {
      buttons.put(menuUtil.getSettingSlot("PARTY_REQUESTS"),
          new SettingUpdateButton(menuUtil.getSettingName("PARTY_REQUESTS"),
              menuUtil.getSettingIcon("PARTY_REQUESTS"), 0,
              Arrays.asList(menuUtil.getSettingLore("PARTY_REQUESTS")), "togglepartyinvites",
              "partyInvites")
      );
    }

    if (menuUtil.isShowingSetting("ALLOW_SPECTATORS")) {
      buttons.put(menuUtil.getSettingSlot("ALLOW_SPECTATORS"),
          new SettingUpdateButton(menuUtil.getSettingName("ALLOW_SPECTATORS"),
              menuUtil.getSettingIcon("ALLOW_SPECTATORS"), 0,
              Arrays.asList(menuUtil.getSettingLore("ALLOW_SPECTATORS")), "togglespectators",
              "spectatorsAllowed")
      );
    }

    if (menuUtil.isShowingSetting("TOGGLE_SCOREBOARD")) {
      buttons.put(menuUtil.getSettingSlot("TOGGLE_SCOREBOARD"),
          new SettingUpdateButton(menuUtil.getSettingName("TOGGLE_SCOREBOARD"),
              menuUtil.getSettingIcon("TOGGLE_SCOREBOARD"), 0,
              Arrays.asList(menuUtil.getSettingLore("TOGGLE_SCOREBOARD")), "togglescoreboard",
              "scoreboardToggled")
      );
    }

    if (menuUtil.isShowingSetting("PING_ON_SCOREBOARD")) {
      buttons.put(menuUtil.getSettingSlot("PING_ON_SCOREBOARD"),
          new SettingUpdateButton(menuUtil.getSettingName("PING_ON_SCOREBOARD"),
              menuUtil.getSettingIcon("PING_ON_SCOREBOARD"), 0,
              Arrays.asList(menuUtil.getSettingLore("PING_ON_SCOREBOARD")), "togglepingscoreboard",
              "pingScoreboardToggled")
      );
    }

    if (menuUtil.isShowingSetting("VANILLA_TAB")) {
      buttons.put(menuUtil.getSettingSlot("VANILLA_TAB"),
          new SettingUpdateButton(menuUtil.getSettingName("VANILLA_TAB"),
              menuUtil.getSettingIcon("VANILLA_TAB"), 0,
              Arrays.asList(menuUtil.getSettingLore("VANILLA_TAB")), "togglevanillatab",
              "vanillaTab")
      );
    }

    if (menuUtil.isShowingSetting("QUEUE_PING_LIMIT")) {
      buttons.put(menuUtil.getSettingSlot("QUEUE_PING_LIMIT"),
          new PingRangeButton(menuUtil.getSettingName("QUEUE_PING_LIMIT"),
              menuUtil.getSettingIcon("QUEUE_PING_LIMIT"), 0,
              Arrays.asList(menuUtil.getSettingLore("QUEUE_PING_LIMIT")),
              plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()))
      );
    }

    if (menuUtil.isShowingSetting("TIME_SETTING")) {
      buttons.put(menuUtil.getSettingSlot("TIME_SETTING"), new SettingUpdateButton(
          menuUtil.getSettingName("TIME_SETTING").replace("<time>",
              plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
                  .getPlayerSettings().getPlayerTime().getName()),
          menuUtil.getSettingIcon("TIME_SETTING"), 0,
          Arrays.asList(menuUtil.getSettingLore("TIME_SETTING")), "", "timeSetting")
      );
    }

    if (menuUtil.isShowingSetting("DEATH_EFFECT_SETTINGS")) {
      buttons.put(menuUtil.getSettingSlot("DEATH_EFFECT_SETTINGS"),
          new SettingUpdateButton(menuUtil.getSettingName("DEATH_EFFECT_SETTINGS"),
              menuUtil.getSettingIcon("DEATH_EFFECT_SETTINGS"), 0,
              Arrays.asList(menuUtil.getSettingLore("DEATH_EFFECT_SETTINGS")), "",
              "deathEffectsSettings")
      );
    }

    return buttons;
  }
}
