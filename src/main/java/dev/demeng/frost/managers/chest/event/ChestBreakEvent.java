package dev.demeng.frost.managers.chest.event;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

@Getter
public class ChestBreakEvent extends Event {

  public static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private PracticePlayerData playerData;
  private final Location chestLocation;
  private final Inventory inventory;

  public ChestBreakEvent(Player player, Inventory inventory, Location location) {
    this.player = player;
    this.chestLocation = location;
    this.inventory = inventory;

    if (Frost.getInstance().getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId()) != null) {
      this.playerData = Frost.getInstance().getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
    }
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
