package dev.demeng.frost.user.ui.queue.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class UnrankedButton extends Button {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "QUEUES.TYPES.UNRANKED");

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();

    for (String text : config.getStringList("LORE")) {
      lore.add(text
          .replace("<queued>", String.valueOf(
              plugin.getManagerHandler().getQueueManager().getQueueSize(QueueType.UNRANKED)))
          .replace("<fighting>", String.valueOf(
              plugin.getManagerHandler().getMatchManager().getFighters(QueueType.UNRANKED)))
      );
    }

    return new ItemBuilder(Material.valueOf(config.getString("ICON")))
        .name(CC.parse(player, config.getString("NAME")))
        .durability(config.getInt("DATA"))
        .lore(CC.parse(player, lore))
        .hideFlags()
        .amount(1)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    player.openInventory(
        plugin.getManagerHandler().getInventoryManager().getUnrankedInventory().getCurrentPage());
  }
}
