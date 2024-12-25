package dev.demeng.frost.util.timer.impl;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.brackets.BracketsEvent;
import dev.demeng.frost.events.games.lms.LMSEvent;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.timer.PlayerTimer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

public class EnderpearlTimer extends PlayerTimer implements Listener {

  private final ConfigCursor configCursor = new ConfigCursor(
      Frost.getInstance().getMessagesConfig(), "MESSAGES.ENDERPEARL");

  public EnderpearlTimer() {
    super("Enderpearl", TimeUnit.SECONDS.toMillis(15));
  }

  @Override
  protected void handleExpiry(Player player, UUID playerUUID) {
    super.handleExpiry(player, playerUUID);

    if (player == null) {
      return;
    }

    CC.sendMessage(player, configCursor.getString("CAN-THROW"));
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if ((event.getAction() != Action.RIGHT_CLICK_BLOCK
        && event.getAction() != Action.RIGHT_CLICK_AIR) || !event.hasItem()) {
      return;
    }

    Player player = event.getPlayer();
    if (event.getItem().getType() == Material.ENDER_PEARL) {
      long cooldown = this.getRemaining(player);
      PracticeEvent<?> practiceEvent = Frost.getInstance().getManagerHandler().getEventManager()
          .getEventPlaying(player);
      if (practiceEvent instanceof LMSEvent || practiceEvent instanceof BracketsEvent
          || practiceEvent instanceof SkyWarsEvent) {
        if (cooldown > 0) {
          event.setCancelled(true);
          CC.sendMessage(player, configCursor.getString("CANNOT-THROW")
              .replace("<pearl_cooldown>",
                  DurationFormatUtils.formatDurationWords(cooldown, true, true))
          );

          player.updateInventory();
          return;
        }
        return;
      }

      PracticePlayerData playerData = Frost.getInstance().getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (playerData.getPlayerState() == PlayerState.FFA) {
        if (cooldown > 0) {
          event.setCancelled(true);
          CC.sendMessage(player, configCursor.getString("CANNOT-THROW")
              .replace("<pearl_cooldown>",
                  DurationFormatUtils.formatDurationWords(cooldown, true, true))
          );
          player.updateInventory();
          return;
        }
        return;
      }

      Match match = Frost.getInstance().getManagerHandler().getMatchManager()
          .getMatch(player.getUniqueId());
      if (match == null) {
        return;
      }
      if (cooldown > 0
          && (!match.getKit().isStickFight()
          || playerData.isInEvent()
          || playerData.getPlayerState() == PlayerState.FFA)) {
        event.setCancelled(true);
        CC.sendMessage(player, configCursor.getString("CANNOT-THROW")
            .replace("<pearl_cooldown>",
                DurationFormatUtils.formatDurationWords(cooldown, true, true))
        );
        player.updateInventory();
      }
    }
  }

  @EventHandler
  public void onPearlLaunch(ProjectileLaunchEvent event) {
    if (event.getEntity().getShooter() instanceof Player
        && event.getEntity() instanceof EnderPearl) {
      Player player = (Player) event.getEntity().getShooter();
      Board board = Board.getByPlayer(player);
      BoardCooldown cooldown = board.getCooldown("enderpearl");

      PracticeEvent<?> practiceEvent = Frost.getInstance().getManagerHandler().getEventManager()
          .getEventPlaying(player);
      if (practiceEvent instanceof LMSEvent || practiceEvent instanceof BracketsEvent
          || practiceEvent instanceof SkyWarsEvent) {
        if (cooldown != null) {
          event.setCancelled(true);
          player.updateInventory();
          return;
        }

        new BoardCooldown(board, "enderpearl", 15);
        this.setCooldown(player, player.getUniqueId());
        return;
      }

      PracticePlayerData playerData = Frost.getInstance().getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (playerData.getPlayerState() == PlayerState.FFA) {
        if (cooldown != null) {
          event.setCancelled(true);
          player.updateInventory();
          return;
        }

        new BoardCooldown(board, "enderpearl", 15);
        this.setCooldown(player, player.getUniqueId());
        return;
      }

      Match match = Frost.getInstance().getManagerHandler().getMatchManager()
          .getMatch(player.getUniqueId());
      if (match == null) {
        return;
      }
      if (!match.getKit().isStickFight() || playerData.isInEvent()
          || playerData.getPlayerState() == PlayerState.FFA) {
        if (cooldown != null) {
          event.setCancelled(true);
          player.updateInventory();
          return;
        }

        new BoardCooldown(board, "enderpearl", 15);
        this.setCooldown(player, player.getUniqueId());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
    if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
      return;
    }

    Player player = event.getPlayer();
    if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
      event.setCancelled(true);
      return;
    }
    if (this.getRemaining(player) != 0 && event.isCancelled()) {
      this.clearCooldown(player);
    }
  }
}
