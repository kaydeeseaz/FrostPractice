package dev.demeng.frost.listeners;

import dev.demeng.frost.user.ui.editor.KitEditorMenu;
import dev.demeng.frost.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class KitEditorListener implements Listener {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getClickedInventory() != event.getView().getTopInventory()) {
      return;
    }

    if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
      return;
    }

    if (Menu.currentlyOpenedMenus.get(event.getWhoClicked().getName()) instanceof KitEditorMenu) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    if (Menu.currentlyOpenedMenus.get(event.getWhoClicked().getName()) instanceof KitEditorMenu) {
      event.setCancelled(true);
    }
  }
}
