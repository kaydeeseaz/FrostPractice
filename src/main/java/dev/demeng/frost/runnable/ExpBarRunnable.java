package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.oitc.OITCEvent;
import dev.demeng.frost.events.games.oitc.OITCPlayer;
import dev.demeng.frost.util.timer.impl.BridgeArrowTimer;
import dev.demeng.frost.util.timer.impl.EnderpearlTimer;
import dev.demeng.frost.util.timer.impl.GlockTimer;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ExpBarRunnable implements Runnable {

  private final Frost plugin;

  @Override
  public void run() {
    EnderpearlTimer enderpearlTimer = plugin.getManagerHandler().getTimerManager()
        .getTimer(EnderpearlTimer.class);
    for (UUID uuid : enderpearlTimer.getCooldowns().keySet()) {
      Player player = plugin.getServer().getPlayer(uuid);
      if (player != null) {
        long time = enderpearlTimer.getRemaining(player);
        int seconds = (int) Math.round((double) time / 1000.0D);
        player.setLevel(seconds);
        player.setExp((float) time / 15000.0F);
      }
    }

    GlockTimer glockTimer = plugin.getManagerHandler().getTimerManager().getTimer(GlockTimer.class);
    for (UUID uuid : glockTimer.getCooldowns().keySet()) {
      Player player = plugin.getServer().getPlayer(uuid);
      if (player != null) {
        long time = glockTimer.getRemaining(player);
        int seconds = (int) Math.round((double) time / 1000.0D);
        player.setLevel(seconds);
        player.setExp((float) time / 1000.0F);
      }
    }

    BridgeArrowTimer bridgeArrowTimer = plugin.getManagerHandler().getTimerManager()
        .getTimer(BridgeArrowTimer.class);
    for (UUID uuid : bridgeArrowTimer.getCooldowns().keySet()) {
      Player player = plugin.getServer().getPlayer(uuid);
      if (player != null) {
        long time = bridgeArrowTimer.getRemaining(player);
        int seconds = (int) Math.round((double) time / 1000.0D);
        player.setLevel(seconds);
        player.setExp((float) time / 4000.0F);
      }
    }

    for (Player player : plugin.getServer().getOnlinePlayers()) {
      PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getEventPlaying(player);
      if (event != null && event instanceof OITCEvent) {
        OITCEvent oitcEvent = (OITCEvent) event;
        OITCPlayer oitcPlayer = oitcEvent.getPlayer(player.getUniqueId());
        if (oitcPlayer != null && oitcPlayer.getState() != OITCPlayer.OITCState.WAITING) {
          int seconds = oitcEvent.getGameTask().getTime();
          if (seconds >= 0) {
            player.setLevel(seconds);
          }
        }
      }
    }
  }
}
