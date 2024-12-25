package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;

public class SpawnPlayerVisibilityRunnable implements Runnable {

  private final Frost plugin = Frost.getInstance();

  @Override
  public void run() {
    if (plugin.getSettingsConfig().getConfig()
        .getBoolean("SETTINGS.OVERRIDE-PLAYER.SHOW-PLAYERS")) {
      plugin.getServer().getOnlinePlayers().forEach(player -> {
        PracticePlayerData playerData = plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(player.getUniqueId());
        if (playerData == null) {
          return;
        }
        if (playerData.isInSpawn() || playerData.isQueueing()) {
          plugin.getServer().getOnlinePlayers()
              .forEach(op -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (op == null) {
                  return;
                }

                PracticePlayerData onlineData = plugin.getManagerHandler().getPlayerManager()
                    .getPlayerData(op.getUniqueId());
                if (onlineData == null) {
                  return;
                }

                if (onlineData.isInSpawn() || onlineData.isQueueing()) {
                  PlayerUtil.hideOrShowPlayer(player, op, false);
                  PlayerUtil.hideOrShowPlayer(op, player, false);
                }
              }));
        } else if (playerData.getPlayerState() == PlayerState.FFA) {
          plugin.getServer().getOnlinePlayers()
              .forEach(op -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (op == null) {
                  return;
                }

                PracticePlayerData onlineData = plugin.getManagerHandler().getPlayerManager()
                    .getPlayerData(op.getUniqueId());
                if (onlineData == null) {
                  return;
                }

                if (onlineData.getPlayerState() != PlayerState.FFA) {
                  PlayerUtil.hideOrShowPlayer(player, op, true);
                  PlayerUtil.hideOrShowPlayer(op, player, true);
                }
              }));
        }
      });
    } else {
      plugin.getServer().getOnlinePlayers().forEach(player -> {
        PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(player.getUniqueId());
        if (practicePlayerData.isInSpawn()) {
          if (practicePlayerData.getPlayerSettings().isPlayerVisibility()) {
            plugin.getServer().getOnlinePlayers()
                .forEach(op -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                  if (op == null) {
                    return;
                  }

                  PracticePlayerData onlineData = plugin.getManagerHandler().getPlayerManager()
                      .getPlayerData(op.getUniqueId());
                  if (onlineData == null) {
                    return;
                  }

                  PlayerUtil.hideOrShowPlayer(player, op,
                      onlineData.getPlayerState() != PlayerState.SPAWN || !op.hasPermission(
                          "frost.user.spawn-visibility"));
                }));
          } else {
            plugin.getServer().getOnlinePlayers()
                .forEach(op -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                  if (op == null) {
                    return;
                  }

                  PracticePlayerData onlineData = plugin.getManagerHandler().getPlayerManager()
                      .getPlayerData(op.getUniqueId());
                  if (onlineData == null) {
                    return;
                  }

                  if (onlineData.getPlayerState() == PlayerState.SPAWN) {
                    PlayerUtil.hideOrShowPlayer(player, op, true);
                  }
                }));
          }
        } else if (practicePlayerData.getPlayerState() == PlayerState.FFA) {
          plugin.getServer().getOnlinePlayers()
              .forEach(op -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (op == null) {
                  return;
                }

                PracticePlayerData onlineData = plugin.getManagerHandler().getPlayerManager()
                    .getPlayerData(op.getUniqueId());
                if (onlineData == null) {
                  return;
                }

                if (onlineData.getPlayerState() != PlayerState.FFA) {
                  PlayerUtil.hideOrShowPlayer(player, op, true);
                  PlayerUtil.hideOrShowPlayer(op, player, true);
                }
              }));
        }
      });
    }
  }
}
