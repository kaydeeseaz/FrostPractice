package dev.demeng.frost.user.ui.editor;

import com.google.common.collect.Lists;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SelectLadderKitMenu extends Menu {

  public SelectLadderKitMenu() {
    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player,
        plugin.getMenusConfig().getConfig().getString("KIT-EDITOR-INVENTORY.TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    plugin.getManagerHandler().getKitManager().getKits().forEach(kit -> {
      if (kit.getKitEditContents()[0] != null) {
        buttons.put(kit.getEditorPos(), new LadderKitDisplayButton(kit));
      }
    });

    return buttons;
  }

  @Override
  public int getSize() {
    return plugin.getMenusConfig().getConfig().getInt("KIT-EDITOR-INVENTORY.SIZE") * 9;
  }

  @AllArgsConstructor
  private class LadderKitDisplayButton extends Button {

    private Kit ladder;

    @Override
    public ItemStack getButtonItem(Player player) {
      List<String> lore = Lists.newArrayList();

      for (String string : plugin.getMenusConfig().getConfig()
          .getStringList("KIT-EDITOR-INVENTORY.LORE")) {
        lore.add(CC.parse(player, string.replace("<kit_name>", this.ladder.getName())));
      }

      return new ItemBuilder(this.ladder.getIcon().getType())
          .name(CC.parse(player,
              plugin.getMenusConfig().getConfig().getString("KIT-EDITOR-INVENTORY.NAME-COLOR"))
              + this.ladder.getName())
          .durability(this.ladder.getIcon().getDurability())
          .lore(lore)
          .hideFlags()
          .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
      player.closeInventory();

      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      practicePlayerData.setSelectedLadder(this.ladder);

      new KitManagementMenu(this.ladder).openMenu(player);
    }
  }
}
