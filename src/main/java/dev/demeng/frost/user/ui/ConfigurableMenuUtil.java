package dev.demeng.frost.user.ui;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class ConfigurableMenuUtil {

  private final Frost plugin;

  private final ConfigCursor hostMenu;
  private final ConfigCursor settingMenu;

  public ConfigurableMenuUtil(Frost plugin) {
    this.plugin = plugin;

    this.hostMenu = new ConfigCursor(plugin.getMenusConfig(), "EVENTS-INVENTORY.EVENTS");
    this.settingMenu = new ConfigCursor(plugin.getMenusConfig(), "SETTINGS-INVENTORY.SETTINGS");
  }

  public String getEventName(String eventName) {
    return CC.color(hostMenu.getString(eventName + ".NAME"));
  }

  public boolean isHostable(String eventName) {
    return hostMenu.getBoolean(eventName + ".SHOW");
  }

  public int getEventSlot(String eventName) {
    return hostMenu.getInt(eventName + ".SLOT");
  }

  public Material getEventIcon(String eventName) {
    return Material.valueOf(hostMenu.getString(eventName + ".ICON"));
  }

  public String[] getEventLore(String eventName) {
    List<String> lore = new ArrayList<>();

    for (String string : hostMenu.getStringList(eventName + ".LORE")) {
      lore.add(CC.color(string));
    }

    return lore.toArray(new String[0]);
  }

  public boolean isShowingSetting(String settingName) {
    return settingMenu.getBoolean(settingName + ".SHOW");
  }

  public String getSettingName(String settingName) {
    return CC.color(settingMenu.getString(settingName + ".NAME"));
  }

  public int getSettingSlot(String settingName) {
    return settingMenu.getInt(settingName + ".SLOT");
  }

  public Material getSettingIcon(String settingName) {
    return Material.valueOf(settingMenu.getString(settingName + ".ICON"));
  }

  public String[] getSettingLore(String settingName) {
    List<String> lore = new ArrayList<>();

    lore.add(
        CC.color(plugin.getMenusConfig().getConfig().getString("SETTINGS-INVENTORY.TOP-SPLITTER")));

    for (String string : settingMenu.getStringList(settingName + ".LORE")) {
      lore.add(CC.color(string));
    }

    return lore.toArray(new String[0]);
  }
}
