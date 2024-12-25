package dev.demeng.frost.commands.admin.stats;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("data set")
@CommandPermission("frost.admin")
public class PlayerStatsCommand {

  @Dependency private Frost plugin;
  private final String NO_KIT = CC.color("&4&lERROR&4! &cThat kit doesn't exist!");

  @Subcommand("playedmatches")
  public void setPlayerStats(Player player, Player target, int amount) {
    plugin.getManagerHandler().getPlayerManager().getPlayerData(target.getUniqueId())
        .setMatchesPlayed(amount);
    player.sendMessage(
        CC.color("&aSuccessfully updated " + target.getName() + "'s played matches to " + amount));
  }

  @Subcommand("elo")
  public void setPlayerElo(Player player, Player target, Kit kit, int elo) {
    if (kit == null) {
      player.sendMessage(NO_KIT);
      return;
    }

    plugin.getManagerHandler().getPlayerManager().getPlayerData(target.getUniqueId())
        .setElo(kit.getName(), elo);
    player.sendMessage(CC.color(
        "&aSuccessfully updated " + target.getName() + "'s " + kit.getName() + " ELO to " + elo));
  }

  @Subcommand("winstreak")
  public void setPlayerWinstreak(Player player, Player target, String type, Kit kit,
      int winstreak) {
    if (kit == null) {
      player.sendMessage(NO_KIT);
      return;
    }

    switch (type.toLowerCase()) {
      case "current":
        plugin.getManagerHandler().getPlayerManager().getPlayerData(target.getUniqueId())
            .setCurrentWinstreak(kit.getName(), winstreak);
        break;
      case "highest":
        plugin.getManagerHandler().getPlayerManager().getPlayerData(target.getUniqueId())
            .setHighestWinStreak(kit.getName(), winstreak);
        break;
    }

    player.sendMessage(CC.color(
        "&aSuccessfully updated " + target.getName() + "'s " + kit.getName() + " " + type
            + " winstreak to " + winstreak));
  }
}
