package dev.demeng.frost.user.ui.editor;

import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.kit.PlayerKit;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import dev.demeng.frost.util.menu.buttons.BackButton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class KitManagementMenu extends Menu {

  private final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 7, " ");

  private final Kit ladder;

  public KitManagementMenu(Kit ladder) {
    this.ladder = ladder;

    this.setPlaceholder(true);
    this.setUpdateAfterClick(true);
  }

  @Override
  public String getTitle(Player player) {
    return "Viewing " + this.ladder.getName() + " kits";
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    PlayerKit[] kits = practicePlayerData.getKits(this.ladder);

    if (kits == null) {
      return buttons;
    }

    int startPos = -1;

    for (int i = 0; i < 4; i++) {
      PlayerKit kit = kits[i];
      startPos += 2;

      buttons.put(startPos, kit == null ? new CreateKitButton(i) : new KitDisplayButton(kit));
      buttons.put(startPos + 18, new LoadKitButton(i));
      buttons.put(startPos + 27, kit == null ? PLACEHOLDER : new RenameKitButton(kit));
      buttons.put(startPos + 36, kit == null ? PLACEHOLDER : new DeleteKitButton(kit));
    }

    buttons.put(36, new BackButton(new SelectLadderKitMenu()));

    return buttons;
  }

  @Override
  public void onClose(Player player) {
    if (!this.isClosedByMenu()) {
      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      practicePlayerData.setSelectedLadder(null);
    }
  }

  @AllArgsConstructor
  private class DeleteKitButton extends Button {

    private PlayerKit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
      return new ItemBuilder(Material.WOOL).name("&c&lDelete").durability(14)
          .lore(Arrays.asList(
              "",
              "&7Click to delete this kit.",
              "&7You will &4&lNOT &7be able",
              "&7to recover this kit."
          ))
          .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      practicePlayerData.deleteKit(practicePlayerData.getSelectedLadder(), this.kit);
      Button.playFail(player);
    }
  }

  @AllArgsConstructor
  private class CreateKitButton extends Button {

    private int index;

    @Override
    public ItemStack getButtonItem(Player player) {
      return new ItemBuilder(Material.IRON_SWORD).name("&a&lCreate Kit").build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      Kit ladder = practicePlayerData.getSelectedLadder();
      if (ladder == null) {
        player.closeInventory();
        return;
      }

      PlayerKit kit = new PlayerKit("Kit " + (this.index + 1), this.index, ladder.getContents(),
          "Kit " + (this.index + 1));
      kit.setContents(practicePlayerData.getSelectedLadder().getContents());
      practicePlayerData.addPlayerKit(this.index, kit);
      practicePlayerData.replaceKit(practicePlayerData.getSelectedLadder(), this.index, kit);
      practicePlayerData.setSelectedKit(kit);

      Button.playSuccess(player);
      new KitEditorMenu(plugin).openMenu(player);
    }
  }

  @AllArgsConstructor
  private class RenameKitButton extends Button {

    private PlayerKit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
      return new ItemBuilder(Material.SIGN).name("&e&lRename")
          .lore(Arrays.asList("", "&aClick to rename this kit.")).build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
      Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      practicePlayerData.setActive(true);
      practicePlayerData.setRename(true);
      practicePlayerData.setSelectedKit(this.kit);

      player.closeInventory();
      Button.playSuccess(player);
      player.sendMessage(CC.color(
          "&cRenaming &l" + this.kit.getName() + "&c... &a" + "Enter the new kit name..."));
    }
  }

  @AllArgsConstructor
  private class LoadKitButton extends Button {

    private int index;

    @Override
    public ItemStack getButtonItem(Player player) {
      return new ItemBuilder(Material.BOOK).name("&a&lLoad/Edit")
          .lore(Arrays.asList("", "&7Click to edit this kit.")).build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (practicePlayerData.getSelectedLadder() == null) {
        player.closeInventory();
        return;
      }

      PlayerKit kit = practicePlayerData.getKit(practicePlayerData.getSelectedLadder(), this.index);
      if (kit == null) {
        kit = new PlayerKit("Kit " + (this.index + 1), this.index, ladder.getContents().clone(),
            "Kit " + (this.index + 1));
        kit.setContents(practicePlayerData.getSelectedLadder().getContents());
        practicePlayerData.addPlayerKit(this.index, kit);
        practicePlayerData.replaceKit(practicePlayerData.getSelectedLadder(), this.index, kit);
      }

      Button.playSuccess(player);
      practicePlayerData.setSelectedKit(kit);
      new KitEditorMenu(plugin).openMenu(player);
    }
  }

  @AllArgsConstructor
  private class KitDisplayButton extends Button {

    private PlayerKit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
      return new ItemBuilder(Material.BOOK).name("&a&l" + this.kit.getName()).build();
    }
  }
}
