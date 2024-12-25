package dev.demeng.frost.game.match.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.managers.ItemManager;
import dev.demeng.frost.user.player.PracticePlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class MenuBugFixListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler
  public void fixMenuBug(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (player.getOpenInventory().getTitle().contains("Editing")) {
      event.setCancelled(true);
      player.closeInventory();
      switch (practicePlayerData.getPlayerState()) {
        case EDITING:
        case SPAWN:
          plugin.getManagerHandler().getPlayerManager().giveLobbyItems(player);
          break;
        case SPECTATING:
          for (ItemManager.HotbarItem item : plugin.getManagerHandler().getItemManager()
              .getSpecItems()) {
            if (item.isEnabled()) {
              player.getInventory().setItem(item.getSlot(), item.getItemStack());
            }
          }
          break;
        case FIGHTING:
          if (plugin.getManagerHandler().getMatchManager().getMatch(practicePlayerData) == null) {
            return;
          }
          Kit kit = plugin.getManagerHandler().getMatchManager().getMatch(practicePlayerData)
              .getKit();
          player.getInventory().setContents(kit.getContents());
          player.getInventory().setArmorContents(kit.getArmor());
          break;
      }

      player.updateInventory();
    }
  }
}
