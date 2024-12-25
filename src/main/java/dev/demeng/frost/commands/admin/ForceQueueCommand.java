package dev.demeng.frost.commands.admin;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class ForceQueueCommand {

  @Dependency private Frost plugin;

  @Command("forcequeue")
  @CommandPermission("frost.admin.force-queue")
  public void execute(Player player, Player target, Kit kit) {
    PracticePlayerData targetData = Frost.getInstance().getManagerHandler().getPlayerManager()
        .getPlayerData(target.getUniqueId());
    if (targetData == null) {
      player.sendMessage(CC.color("&cThere is no data stored for that player."));
      return;
    }

    if (kit == null) {
      player.sendMessage(CC.color("&cThat kit does not exist."));
      return;
    }

    plugin.getManagerHandler().getQueueManager()
        .addPlayerToQueue(target, targetData, kit.getName(), QueueType.UNRANKED);
    player.sendMessage(CC.color(
        "&aYou have forcefully added " + target.getName() + " into the " + kit.getName()
            + " queue."));
    target.sendMessage(CC.color(
        "&7&oYou have been forcefully added into the " + kit.getName() + " queue by "
            + player.getName()));
  }
}
