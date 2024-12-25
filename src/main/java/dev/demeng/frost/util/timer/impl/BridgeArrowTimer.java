package dev.demeng.frost.util.timer.impl;

import static dev.demeng.frost.game.match.listeners.SpecialMatchListener.isOnBridge;
import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.timer.PlayerTimer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BridgeArrowTimer extends PlayerTimer implements Listener {

  private final ConfigCursor configCursor = new ConfigCursor(
      Frost.getInstance().getMessagesConfig(), "MESSAGES.BRIDGES-ARROW");

  public BridgeArrowTimer() {
    super("Arrow", TimeUnit.SECONDS.toMillis(4));
  }

  @Override
  protected void handleExpiry(Player player, UUID playerUUID) {
    super.handleExpiry(player, playerUUID);

    if (player == null) {
      return;
    }

    sendMessage(player, configCursor.getString("CAN-SHOOT"));
    if (!player.getInventory().contains(Material.ARROW)) {
      player.getInventory().addItem(new ItemStack(Material.ARROW));
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if ((event.getAction() != Action.RIGHT_CLICK_BLOCK
        && event.getAction() != Action.RIGHT_CLICK_AIR) || !event.hasItem()) {
      return;
    }

    Player player = event.getPlayer();
    if (event.getItem().getType() == Material.ARROW || event.getItem().getType() == Material.BOW) {
      long cooldown = this.getRemaining(player);
      if (cooldown > 0) {
        event.setCancelled(true);
        sendMessage(player, configCursor.getString("CANNOT-SHOOT")
            .replace("<arrow_cooldown>",
                DurationFormatUtils.formatDurationWords(cooldown, true, true))
        );

        player.updateInventory();
      }
    }
  }

  @EventHandler
  public void onArrowShoot(ProjectileLaunchEvent event) {
    if (event.getEntity().getShooter() instanceof Player
        && event.getEntity() instanceof Arrow) {
      Player player = (Player) event.getEntity().getShooter();
      Board board = Board.getByPlayer(player);
      BoardCooldown cooldown = board.getCooldown("arrow");

      if (!isOnBridge(player)) {
        return;
      }
      if (cooldown != null) {
        event.setCancelled(true);
        player.updateInventory();
        return;
      }

      new BoardCooldown(board, "arrow", 4);
      this.setCooldown(player, player.getUniqueId());
    }
  }
}
