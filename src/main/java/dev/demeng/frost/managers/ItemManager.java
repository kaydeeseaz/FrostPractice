package dev.demeng.frost.managers;

import com.google.common.collect.Lists;
import dev.demeng.frost.Frost;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public class ItemManager {

  private final List<HotbarItem> spawnItems = new ArrayList<>();
  private final List<HotbarItem> queueItems = new ArrayList<>();
  private final List<HotbarItem> partyItems = new ArrayList<>();
  private final List<HotbarItem> partySpecItems = new ArrayList<>();
  private final List<HotbarItem> tournamentItems = new ArrayList<>();
  private final List<HotbarItem> eventItems = new ArrayList<>();
  private final List<HotbarItem> parkourItems = new ArrayList<>();
  private final List<HotbarItem> specItems = new ArrayList<>();

  private final HotbarItem playAgainItem;
  private final ItemStack defaultBook;
  private ConfigCursor hotbar;

  public ItemManager() {
    this.defaultBook = ItemUtil.createItem(Material.ENCHANTED_BOOK,
        CC.color(Frost.getInstance().getHotbarConfig().getConfig().getString("DEFAULT-KIT")));

    ConfigCursor cursor = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "PLAY-AGAIN");
    this.playAgainItem = (new HotbarItem(ItemUtil.createUnbreakableItem(
        Material.valueOf(cursor.getString("MATERIAL")),
        CC.color(cursor.getString("NAME")),
        cursor.getInt("AMOUNT"),
        (short) cursor.getInt("DATA")),
        cursor.getInt("SLOT"),
        cursor.getBoolean("ENABLED"),
        "PLAY_AGAIN",
        null
    ));

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "IN-SPAWN");
      for (String spawnItem : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("IN-SPAWN").getKeys(false)) {
        spawnItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(spawnItem + ".MATERIAL")),
            CC.color(hotbar.getString(spawnItem + ".NAME")),
            hotbar.getInt(spawnItem + ".AMOUNT"),
            (short) hotbar.getInt(spawnItem + ".DATA")),
            hotbar.getInt(spawnItem + ".SLOT"),
            hotbar.getBoolean(spawnItem + ".ENABLED"),
            hotbar.getString(spawnItem + ".ACTION"),
            hotbar.getString(spawnItem + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      e.printStackTrace();
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Spawn Items. [!]");
    }

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "IN-QUEUE");
      for (String queueItem : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("IN-QUEUE").getKeys(false)) {
        queueItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(queueItem + ".MATERIAL")),
            CC.color(hotbar.getString(queueItem + ".NAME")),
            hotbar.getInt(queueItem + ".AMOUNT"),
            (short) hotbar.getInt(queueItem + ".DATA")),
            hotbar.getInt(queueItem + ".SLOT"),
            hotbar.getBoolean(queueItem + ".ENABLED"),
            hotbar.getString(queueItem + ".ACTION"),
            hotbar.getString(queueItem + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Queue Items. [!]");
    }

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "IN-PARTY");
      for (String partyItem : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("IN-PARTY").getKeys(false)) {
        partyItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(partyItem + ".MATERIAL")),
            CC.color(hotbar.getString(partyItem + ".NAME")),
            hotbar.getInt(partyItem + ".AMOUNT"),
            (short) hotbar.getInt(partyItem + ".DATA")),
            hotbar.getInt(partyItem + ".SLOT"),
            hotbar.getBoolean(partyItem + ".ENABLED"),
            hotbar.getString(partyItem + ".ACTION"),
            hotbar.getString(partyItem + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Party Items. [!]");
    }

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "IN-PARTY-SPEC");
      for (String partyItemSpec : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("IN-PARTY-SPEC").getKeys(false)) {
        partySpecItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(partyItemSpec + ".MATERIAL")),
            CC.color(hotbar.getString(partyItemSpec + ".NAME")),
            hotbar.getInt(partyItemSpec + ".AMOUNT"),
            (short) hotbar.getInt(partyItemSpec + ".DATA")),
            hotbar.getInt(partyItemSpec + ".SLOT"),
            hotbar.getBoolean(partyItemSpec + ".ENABLED"),
            hotbar.getString(partyItemSpec + ".ACTION"),
            hotbar.getString(partyItemSpec + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Party (Spectator) Items. [!]");
    }

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "IN-TOURNAMENT");
      for (String tournamentItem : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("IN-TOURNAMENT").getKeys(false)) {
        tournamentItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(tournamentItem + ".MATERIAL")),
            CC.color(hotbar.getString(tournamentItem + ".NAME")),
            hotbar.getInt(tournamentItem + ".AMOUNT"),
            (short) hotbar.getInt(tournamentItem + ".DATA")),
            hotbar.getInt(tournamentItem + ".SLOT"),
            hotbar.getBoolean(tournamentItem + ".ENABLED"),
            hotbar.getString(tournamentItem + ".ACTION"),
            hotbar.getString(tournamentItem + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Tournament Items. [!]");
    }

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "IN-EVENT");
      for (String eventItem : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("IN-EVENT").getKeys(false)) {
        eventItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(eventItem + ".MATERIAL")),
            CC.color(hotbar.getString(eventItem + ".NAME")),
            hotbar.getInt(eventItem + ".AMOUNT"),
            (short) hotbar.getInt(eventItem + ".DATA")),
            hotbar.getInt(eventItem + ".SLOT"),
            hotbar.getBoolean(eventItem + ".ENABLED"),
            hotbar.getString(eventItem + ".ACTION"),
            hotbar.getString(eventItem + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Event Items. [!]");
    }

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "PARKOUR-EVENT");
      for (String parkourItem : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("PARKOUR-EVENT").getKeys(false)) {
        parkourItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(parkourItem + ".MATERIAL")),
            CC.color(hotbar.getString(parkourItem + ".NAME")),
            hotbar.getInt(parkourItem + ".AMOUNT"),
            (short) hotbar.getInt(parkourItem + ".DATA")),
            hotbar.getInt(parkourItem + ".SLOT"),
            hotbar.getBoolean(parkourItem + ".ENABLED"),
            hotbar.getString(parkourItem + ".ACTION"),
            hotbar.getString(parkourItem + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Parkour Items. [!]");
    }

    try {
      hotbar = new ConfigCursor(Frost.getInstance().getHotbarConfig(), "IN-SPEC");
      for (String specItem : Frost.getInstance().getHotbarConfig().getConfig()
          .getConfigurationSection("IN-SPEC").getKeys(false)) {
        specItems.add(new HotbarItem(ItemUtil.createUnbreakableItem(
            Material.valueOf(hotbar.getString(specItem + ".MATERIAL")),
            CC.color(hotbar.getString(specItem + ".NAME")),
            hotbar.getInt(specItem + ".AMOUNT"),
            (short) hotbar.getInt(specItem + ".DATA")),
            hotbar.getInt(specItem + ".SLOT"),
            hotbar.getBoolean(specItem + ".ENABLED"),
            hotbar.getString(specItem + ".ACTION"),
            hotbar.getString(specItem + ".COMMAND")
        ));
      }
    } catch (Exception e) {
      Frost.getInstance().getLogger()
          .info("[!] An error occurred while trying to register Spectator Items. [!]");
    }
  }

  @Getter
  @Setter
  public static class HotbarItem {

    private static List<HotbarItem> items = Lists.newArrayList();
    private ItemStack itemStack;
    private int slot;
    private boolean enabled;
    private ActionType action;
    private String command;

    public HotbarItem(ItemStack stack, int slot, boolean enabled, String action, String command) {
      this.itemStack = stack;
      this.slot = slot;
      this.enabled = enabled;
      this.action = ActionType.valueOf(action);
      this.command = command;

      items.add(this);
    }

    public static HotbarItem getItemByItem(ItemStack itemStack) {
      return items.stream().filter(item -> item.getItemStack().isSimilar(itemStack)).findFirst()
          .orElse(null);
    }

    public enum ActionType {
      JOIN_UNRANKED, JOIN_RANKED, JOIN_PREMIUM, JOIN_FFA, CREATE_PARTY, LEADERBOARDS_MENU, EDITOR_MENU, SETTINGS_MENU, LEAVE_QUEUE, EVENTS_MENU, QUEUES_MENU,
      JOIN_UNRANKED_2V2, PARTY_INFO, OTHER_PARTIES, PARTY_EVENTS, PARTY_LEAVE,
      LEAVE_SPECTATOR, PLAYERS_MENU, OPEN_CURRENT_MATCHES, PLAY_AGAIN, EXECUTABLE_COMMAND, HIDE_PLAYERS, LEAVE_EVENT, LEAVE_TOURNAMENT
    }
  }
}
