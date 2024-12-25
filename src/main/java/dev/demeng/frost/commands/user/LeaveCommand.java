package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;

public class LeaveCommand {

  @Dependency private Frost plugin;

  @Command("leave")
  public void leaveEvent(Player player, PracticePlayerData practicePlayerData) {
    switch (practicePlayerData.getPlayerState()) {
      case FFA:
        if (!plugin.getManagerHandler().getFfaManager().getAllPlayers()
            .contains(player.getUniqueId())) {
          player.sendMessage(CC.color("&cYou are not in FFA."));
          return;
        }

        plugin.getManagerHandler().getFfaManager().getByPlayer(player).removePlayer(player);
        break;
      case EVENT:
        if (plugin.getManagerHandler().getEventManager().getEventPlaying(player) != null) {
          PracticeEvent<?> event = plugin.getManagerHandler().getEventManager()
              .getEventPlaying(player);
          if (!plugin.getManagerHandler().getEventManager().isPlaying(player, event)) {
            player.sendMessage(CC.color("&cYou are not in an event."));
            return;
          }

          event.leave(player);
        }
        break;
      default:
        player.sendMessage(CC.color("&cThere is nothing to leave."));
        break;
    }
  }
}
