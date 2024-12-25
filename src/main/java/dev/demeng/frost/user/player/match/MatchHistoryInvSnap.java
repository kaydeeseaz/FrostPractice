package dev.demeng.frost.user.player.match;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.inventory.InventoryUI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Getter
public class MatchHistoryInvSnap {

  private final ConfigCursor menu = new ConfigCursor(Frost.getInstance().getMenusConfig(),
      "POST-MATCH-INVENTORY");

  private InventoryUI winnerInventory;
  private InventoryUI loserInventory;

  public MatchHistoryInvSnap(MatchLocatedData locatedData) {
    winnerInv(locatedData);
    loserInv(locatedData);
  }

  private void winnerInv(MatchLocatedData locatedData) {
    ItemStack[] contents = locatedData.getWinnerContents();
    ItemStack[] armor = locatedData.getWinnerArmor();
    this.winnerInventory = new InventoryUI(
        Bukkit.getOfflinePlayer(locatedData.getWinnerUUID()).getName() + "'s Inventory", true, 6);

    for (int i = 0; i < 9; i++) {
      this.winnerInventory.setItem(i + 27, new InventoryUI.EmptyClickableItem(contents[i]));
      this.winnerInventory.setItem(i + 18, new InventoryUI.EmptyClickableItem(contents[i + 27]));
      this.winnerInventory.setItem(i + 9, new InventoryUI.EmptyClickableItem(contents[i + 18]));
      this.winnerInventory.setItem(i, new InventoryUI.EmptyClickableItem(contents[i + 9]));
    }

    for (int i = 36; i < 40; i++) {
      this.winnerInventory.setItem(i, new InventoryUI.EmptyClickableItem(armor[39 - i]));
    }

    this.winnerInventory.setItem(45, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(
        ItemUtil.createItem(Material.PAPER, CC.color(menu.getString("MATCH.NEXT-INVENTORY.NAME"))),
        CC.color(menu.getString("MATCH.NEXT-INVENTORY.LORE")))) {
      @Override
      public void onClick(InventoryClickEvent inventoryClickEvent) {
        Player clicker = (Player) inventoryClickEvent.getWhoClicked();
        clicker.openInventory(loserInventory.getCurrentPage());
      }
    });
    this.winnerInventory.setItem(53, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(
        ItemUtil.createItem(Material.PAPER, CC.color(menu.getString("MATCH.NEXT-INVENTORY.NAME"))),
        CC.color(menu.getString("MATCH.NEXT-INVENTORY.LORE")))) {
      @Override
      public void onClick(InventoryClickEvent inventoryClickEvent) {
        Player clicker = (Player) inventoryClickEvent.getWhoClicked();
        clicker.openInventory(loserInventory.getCurrentPage());
      }
    });
  }

  private void loserInv(MatchLocatedData locatedData) {
    ItemStack[] contents = locatedData.getLoserContents();
    ItemStack[] armor = locatedData.getLoserArmor();
    this.loserInventory = new InventoryUI(
        Bukkit.getOfflinePlayer(locatedData.getLoserUUID()).getName() + "'s Inventory", true, 6);

    for (int i = 0; i < 9; i++) {
      this.loserInventory.setItem(i + 27, new InventoryUI.EmptyClickableItem(contents[i]));
      this.loserInventory.setItem(i + 18, new InventoryUI.EmptyClickableItem(contents[i + 27]));
      this.loserInventory.setItem(i + 9, new InventoryUI.EmptyClickableItem(contents[i + 18]));
      this.loserInventory.setItem(i, new InventoryUI.EmptyClickableItem(contents[i + 9]));
    }
    for (int i = 36; i < 40; i++) {
      this.loserInventory.setItem(i, new InventoryUI.EmptyClickableItem(armor[39 - i]));
    }

    this.loserInventory.setItem(45, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(
        ItemUtil.createItem(Material.PAPER, CC.color(menu.getString("MATCH.NEXT-INVENTORY.NAME"))),
        CC.color(menu.getString("MATCH.NEXT-INVENTORY.LORE")))) {
      @Override
      public void onClick(InventoryClickEvent inventoryClickEvent) {
        Player clicker = (Player) inventoryClickEvent.getWhoClicked();
        clicker.openInventory(winnerInventory.getCurrentPage());
      }
    });
    this.loserInventory.setItem(53, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(
        ItemUtil.createItem(Material.PAPER, CC.color(menu.getString("MATCH.NEXT-INVENTORY.NAME"))),
        CC.color(menu.getString("MATCH.NEXT-INVENTORY.LORE")))) {
      @Override
      public void onClick(InventoryClickEvent inventoryClickEvent) {
        Player clicker = (Player) inventoryClickEvent.getWhoClicked();
        clicker.openInventory(winnerInventory.getCurrentPage());
      }
    });
  }
}
