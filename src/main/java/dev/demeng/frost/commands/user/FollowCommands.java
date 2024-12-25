package dev.demeng.frost.commands.user;

import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@CommandPermission("frost.staff.follow")
public class FollowCommands {

  @Dependency
  private Frost plugin;

  @Command("follow")
  public void followPlayer(Player player, Player target) {
    if (target == player) {
      sendMessage(player,
          plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.CANT-FOLLOW-YOURSELF"));
      return;
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager()
        .getParty(practicePlayerData.getUniqueId());
    if (party != null || practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return;
    }

    if (practicePlayerData.getFollowingId() != null && practicePlayerData.getFollowingId()
        .equals(target.getUniqueId())) {
      sendMessage(player,
          plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.ALREADY-FOLLOWING")
              .replace("<player>", target.getName()));
      return;
    }

    if (target == null) {
      return;
    }

    practicePlayerData.setFollowing(true);
    practicePlayerData.setFollowingId(target.getUniqueId());
    sendMessage(player,
        plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.FOLLOWING-PLAYER")
            .replace("<player>", target.getName()));
  }

  @Command("unfollow")
  public void unfollowPlayer(Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (!practicePlayerData.isFollowing() || practicePlayerData.getFollowingId() == null) {
      sendMessage(player,
          plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.FOLLOWING-NOBODY"));
      return;
    }

    sendMessage(player,
        plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.STOPPED-FOLLOWING")
            .replace("<player>", Bukkit.getPlayer(practicePlayerData.getFollowingId()).getName())
    );

    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);
    practicePlayerData.setFollowing(false);
    practicePlayerData.setFollowingId(null);
  }
}
