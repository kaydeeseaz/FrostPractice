package dev.demeng.frost.commands.admin.stats;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class ResetCommand {

  @Dependency private Frost plugin;

  @Command("reset")
  @CommandPermission("frost.admin")
  public void resetStats(Player player, Player target) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(target.getUniqueId());
    for (Kit kit : plugin.getManagerHandler().getKitManager().getKits()) {
      practicePlayerData.setElo(kit.getName(), PracticePlayerData.DEFAULT_ELO);
      practicePlayerData.setHighestWinStreak(kit.getName(), 0);
      practicePlayerData.setLosses(kit.getName(), 0);
      practicePlayerData.setWins(kit.getName(), 0);
      practicePlayerData.setMatchesPlayed(0);
    }

    practicePlayerData.setGlobalHighestWinStreak(0);
    practicePlayerData.setPremiumElo(PracticePlayerData.DEFAULT_ELO);
    practicePlayerData.setGlobalElo(PracticePlayerData.DEFAULT_ELO);

    player.sendMessage(CC.color("&a" + target.getName() + "'s stats have been wiped."));
  }
}
