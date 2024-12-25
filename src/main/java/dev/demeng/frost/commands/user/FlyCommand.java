package dev.demeng.frost.commands.user;

import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class FlyCommand {

  @Dependency private Frost plugin;

  @Command({"fly", "flight"})
  @CommandPermission("frost.user.fly")
  public void toggleFlight(Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return;
    }

    player.setAllowFlight(!player.getAllowFlight());
    sendMessage(player, player.getAllowFlight()
        ? plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.SETTINGS.FLY-ENABLED")
        : plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.SETTINGS.FLY-DISABLED")
    );
  }
}
