package dev.demeng.frost.user.ui.queue.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.user.ui.queue.ffa.FFASelectionMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class FFAButton extends Button {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(), "QUEUES.TYPES.FFA");

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();

    for (String text : config.getStringList("LORE")) {
      lore.add(text.replace("<players>",
          String.valueOf(plugin.getManagerHandler().getFfaManager().getAllPlayers().size()))
      );
    }

    return new ItemBuilder(Material.valueOf(config.getString("ICON")))
        .name(CC.parse(player, config.getString("NAME")))
        .durability(config.getInt("DATA"))
        .lore(CC.color(lore))
        .amount(1)
        .hideFlags()
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    new FFASelectionMenu(plugin.getManagerHandler().getFfaManager()).openMenu(player);
  }
}
