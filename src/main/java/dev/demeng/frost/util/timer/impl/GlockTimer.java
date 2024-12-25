package dev.demeng.frost.util.timer.impl;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.gulag.GulagEvent;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.timer.PlayerTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GlockTimer extends PlayerTimer implements Listener {

  private final Frost plugin = Frost.getInstance();
  private final List<Integer> bullets = new ArrayList<>();

  private final ConfigCursor configCursor = new ConfigCursor(
      Frost.getInstance().getMessagesConfig(), "MESSAGES.WEAPONS");

  public GlockTimer() {
    super("Glock", TimeUnit.SECONDS.toMillis(1));
  }

  @Override
  protected void handleExpiry(Player player, UUID playerUUID) {
    super.handleExpiry(player, playerUUID);

    if (player == null) {
      return;
    }

    CC.sendMessage(player, configCursor.getString("CAN-SHOOT"));
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if ((event.getAction() != Action.RIGHT_CLICK_BLOCK
        && event.getAction() != Action.RIGHT_CLICK_AIR) || !event.hasItem()) {
      return;
    }

    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
        .getEventPlaying(player);
    if (practicePlayerData.isInEvent() && practiceEvent instanceof GulagEvent) {
      if (player.getItemInHand().getType() == Material.IRON_HOE) {
        long cooldown = this.getRemaining(player);
        if (cooldown > 0) {
          event.setCancelled(true);
          CC.sendMessage(player, configCursor.getString("CANNOT-SHOOT")
              .replace("<weapon_cooldown>",
                  DurationFormatUtils.formatDurationWords(cooldown, true, true))
          );
        }

        Arrow bullet = player.launchProjectile(Arrow.class, player.getLocation().getDirection());
        bullets.add(bullet.getEntityId());
        bullet.setVelocity(player.getEyeLocation().getDirection().multiply(4));
      }
    }
  }

  @EventHandler
  public void onGlockShoot(ProjectileLaunchEvent event) {
    if (event.getEntity().getShooter() instanceof Player) {
      if (event.getEntity() instanceof Arrow) {
        Player player = (Player) event.getEntity().getShooter();
        Board board = Board.getByPlayer(player);
        BoardCooldown cooldown = board.getCooldown("glock");
        PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
            .getEventPlaying(player);
        if (practiceEvent instanceof GulagEvent) {
          if (cooldown != null) {
            event.setCancelled(true);
            player.updateInventory();
            return;
          }
          new BoardCooldown(board, "glock", 1);
          this.setCooldown(player, player.getUniqueId());
          Bukkit.getWorld("event").playSound(player.getLocation(), Sound.CLICK, 1.5F, 2.0F);
        }
      }
    }
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent event) {
    if (event.getEntity() instanceof Arrow) {
      int entityId = event.getEntity().getEntityId();
      if (bullets.contains(entityId)) {
        bullets.remove((Integer) entityId);
      }
    }
  }
}
