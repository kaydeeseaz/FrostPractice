package dev.demeng.frost.commands.user;

import static dev.demeng.frost.util.CC.color;
import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Usage;

public class QueueCommand {

  @Dependency private Frost plugin;

  @Command({"queue", "joinqueue"})
  @Usage("/queue <ranked/unranked> <kit>")
  public void queuePlayer(Player player, PracticePlayerData practicePlayerData, String queueType,
      String kitName) {

    if (!practicePlayerData.isInSpawn() || practicePlayerData.isInTournament()) {
      sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return;
    }

    if (plugin.getSettingsConfig().getConfig().getBoolean("SETTINGS.GENERAL.PREMIUM-ENABLED")
        && queueType.equalsIgnoreCase("premium")) {
      player.sendMessage(color("&cPremium is currently disabled."));
      return;
    }

    if (plugin.getManagerHandler().getKitManager().getKit(kitName) == null) {
      player.sendMessage(CC.color("&cThat kit does not exist."));
      return;
    }

    plugin.getManagerHandler().getQueueManager()
        .addPlayerToQueue(player, practicePlayerData, kitName,
            QueueType.valueOf(queueType.toUpperCase()));
  }
}
