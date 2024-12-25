package dev.demeng.frost.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.brackets.BracketsEvent;
import dev.demeng.frost.events.games.knockout.KnockoutEvent;
import dev.demeng.frost.events.games.lms.LMSEvent;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.events.games.tnttag.TNTTagEvent;
import dev.demeng.frost.user.player.PracticePlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;

public class InventoryListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInventoryClick(InventoryClickEvent event) {
    Inventory clicked = event.getClickedInventory();
    if (clicked == null) {
      return;
    }

    Player player = (Player) event.getWhoClicked();
    if (!clicked.equals(player.getInventory())) {
      if (clicked instanceof CraftingInventory) {
        event.setCancelled(true);
        player.updateInventory();
        return;
      }
      return;
    }

    if (player.getGameMode() == GameMode.CREATIVE && player.isOp()) {
      event.setCancelled(false);
      return;
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    switch (practicePlayerData.getPlayerState()) {
      case FIGHTING:
      case FFA:
        event.setCancelled(false);
        break;
      case EDITING:
      case QUEUE:
      case SPAWN:
        if (player.getGameMode() == GameMode.SURVIVAL) {
          if (practicePlayerData.isActive()) {
            if (clicked.equals(player.getOpenInventory().getTopInventory())) {
              if (event.getCursor().getType() != Material.AIR
                  && event.getCurrentItem().getType() == Material.AIR
                  || event.getCursor().getType() != Material.AIR
                  && event.getCurrentItem().getType() != Material.AIR) {
                event.setCancelled(true);
                event.setCursor(null);
                player.updateInventory();
              }
            }
          } else {
            event.setCancelled(true);
          }
        }
        break;
      case EVENT:
        PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
            .getEventPlaying(player);
        if (practiceEvent instanceof TNTTagEvent || practiceEvent instanceof KnockoutEvent) {
          event.setCancelled(true);
        } else if (practiceEvent instanceof BracketsEvent || practiceEvent instanceof LMSEvent
            || practiceEvent instanceof SkyWarsEvent) {
          event.setCancelled(false);
        }
        break;
      case LOADING:
      case SPECTATING:
        event.setCancelled(true);
        break;
    }
  }
}
