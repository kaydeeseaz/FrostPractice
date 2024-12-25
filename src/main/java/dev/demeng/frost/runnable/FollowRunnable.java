package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class FollowRunnable implements Runnable {

  private final Frost plugin;

  @Override
  public void run() {
    plugin.getManagerHandler().getPlayerManager().getAllData().forEach(playerData -> {
      if (playerData == null) {
        return;
      }
      if (playerData.getFollowingId() == null) {
        return;
      }
      if (playerData.getPlayerState() == PlayerState.SPECTATING) {
        return;
      }

      Player target = plugin.getServer().getPlayer(playerData.getFollowingId());
      if (target == null || !target.isOnline()) {
        playerData.setFollowing(false);
        playerData.setFollowingId(null);
        return;
      }

      PracticePlayerData targetData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(playerData.getFollowingId());
      if (targetData == null || targetData.getPlayerState() != PlayerState.FIGHTING) {
        return;
      }

      Match match = plugin.getManagerHandler().getMatchManager().getMatch(targetData);
      if (match == null) {
        return;
      }

      Player player = plugin.getServer().getPlayer(playerData.getUniqueId());
      plugin.getServer().getScheduler()
          .runTask(plugin, () -> player.performCommand("spectate " + target.getName()));
    });
  }
}
