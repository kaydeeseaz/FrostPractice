package dev.demeng.frost.user.ui.settings.matchmaking;

import static dev.demeng.frost.util.CC.color;
import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PingRangeButton extends Button {

  private final String name;
  private final Material material;
  private final int durability;
  private final List<String> lore;

  private final PracticePlayerData practicePlayerData;
  private final int currentPing;

  private final List<Integer> ranges = Arrays.asList(5, 10, 25, 50, 75, 100, 125, 150, 175, 200,
      225, 250, 275, 300, -1);

  private final ConfigCursor cursor;

  public PingRangeButton(String name, Material material, int durability, List<String> lore,
      PracticePlayerData practicePlayerData) {
    this.name = name;
    this.material = material;
    this.durability = durability;
    this.lore = lore;

    this.practicePlayerData = practicePlayerData;
    this.currentPing = practicePlayerData.getPlayerSettings().getPingRange();

    this.cursor = new ConfigCursor(plugin.getMenusConfig(),
        "SETTINGS-INVENTORY.SETTINGS.QUEUE_PING_LIMIT");
  }

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lines = new ArrayList<>();

    String format = practicePlayerData.getPlayerSettings().getPingRange() == -1
        || practicePlayerData.getPlayerSettings().getPingRange() == 0 ? "Unrestricted"
        : "±" + practicePlayerData.getPlayerSettings().getPingRange();
    for (String s : this.lore) {
      lines.add(s.replace("<formatted>",
          cursor.getString("PING-RANGE-FORMAT").replace("<format>", format)));
    }

    lines.add(
        color(plugin.getMenusConfig().getConfig().getString("SETTINGS-INVENTORY.BOTTOM-SPLITTER")));

    return new ItemBuilder(material).name(CC.parse(player, name)).amount(1).lore(lines)
        .durability(durability).build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    if (!player.hasPermission("frost.vip.ping_range")) {
      sendMessage(player,
          plugin.getMenusConfig().getConfig().getString("SETTINGS-INVENTORY.NO-PERMISSION"));
      return;
    }

    int min = ranges.indexOf(ranges.get(0));
    int max = ranges.indexOf(ranges.get(ranges.size() - 1));
    int index = ranges.indexOf(currentPing);

    if (clickType == ClickType.LEFT) {
      if (index >= max) {
        index = min;
      } else {
        index++;
      }
    } else if (clickType == ClickType.RIGHT) {
      if (index <= min) {
        index = max;
      } else {
        index--;
      }
    } else if (clickType == ClickType.SHIFT_LEFT) {
      index = max;
    }

    playSuccess(player);
    practicePlayerData.getPlayerSettings().setPingRange(ranges.get(index));
  }
}
