package dev.demeng.frost.user.ui.queue.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class RankedButton extends Button {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "QUEUES.TYPES.RANKED");

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();

    for (String text : config.getStringList("LORE")) {
      lore.add(text
          .replace("<queued>", String.valueOf(
              plugin.getManagerHandler().getQueueManager().getQueueSize(QueueType.RANKED)))
          .replace("<fighting>", String.valueOf(
              plugin.getManagerHandler().getMatchManager().getFighters(QueueType.RANKED)))
      );
    }

    return new ItemBuilder(Material.valueOf(config.getString("ICON")))
        .name(CC.parse(player, config.getString("NAME")))
        .durability(config.getInt("DATA"))
        .lore(CC.color(lore))
        .hideFlags()
        .amount(1)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    if (PlayerUtil.getPing(player) >= plugin.getSettingsConfig().getConfig()
        .getInt("SETTINGS.MATCH.MAX-RANKED-PING")) {
      CC.sendMessage(player,
          plugin.getSettingsConfig().getConfig().getString("SETTINGS.MATCH.PING-TOO-HIGH-MESSAGE"));
      return;
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (plugin.getSettingsConfig().getConfig().getInt("SETTINGS.MATCH.RANKEDS-REQUIRED") >= 1) {
      if (practicePlayerData.getMatchesPlayed() >= plugin.getSettingsConfig().getConfig()
          .getInt("SETTINGS.MATCH.RANKEDS-REQUIRED")) {
        player.openInventory(
            plugin.getManagerHandler().getInventoryManager().getRankedInventory().getCurrentPage());
      } else if (player.hasPermission("frost.bypass.ranked")) {
        player.openInventory(
            plugin.getManagerHandler().getInventoryManager().getRankedInventory().getCurrentPage());
      } else {
        player.sendMessage(CC.color("&cYou need to play " + (
            plugin.getSettingsConfig().getConfig().getInt("SETTINGS.MATCH.RANKEDS-REQUIRED")
                - practicePlayerData.getMatchesPlayed())
            + " unranked matches before playing ranked!"));
      }
    } else {
      player.openInventory(
          plugin.getManagerHandler().getInventoryManager().getRankedInventory().getCurrentPage());
    }
  }
}
