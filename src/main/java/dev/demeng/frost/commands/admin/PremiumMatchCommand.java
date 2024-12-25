package dev.demeng.frost.commands.admin;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("premium")
@CommandPermission("frost.admin.premium")
public class PremiumMatchCommand {

  @Dependency private Frost plugin;

  @DefaultFor("premium")
  public void getHelpMessage(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()
        + " &8- &fPremium - Command Help"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&7 • &b/premium check <player>"));
    player.sendMessage(CC.color("&7 • &b/premium reset <player>"));
    player.sendMessage(CC.color("&7 • &b/premium set <player> <amount>"));
    player.sendMessage(CC.color("&7 • &b/premium add <player> <amount>"));
    player.sendMessage(CC.color("&7 • &b/premium remove <player> <amount>"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("check")
  public void checkPlayerPremiumMatches(Player player, Player target,
      PracticePlayerData targetData) {
    for (String message : Frost.getInstance().getMessagesConfig().getConfig()
        .getStringList("MESSAGES.PLAYER.CHECK-PLAYER-PREMIUM-MATCHES")) {
      player.sendMessage(CC.color(message
          .replace("<premium_matches>", String.valueOf(targetData.getPremiumMatches()))
          .replace("<player>", target.getName())
      ));
    }
  }

  @Subcommand("set")
  public void setPlayerPremiumMatches(Player player, Player target, PracticePlayerData targetData,
      int amount) {
    targetData.setPremiumMatches(amount);
    player.sendMessage(
        CC.color("&aPremium Matches amount set to " + amount + " for " + target.getName()));
    target.sendMessage(CC.color(
        "&aYour Premium Matches amount has been set to " + amount + " by " + player.getName()));
  }

  @Subcommand("reset")
  public void resetPlayerPremiumMatches(Player player, Player target,
      PracticePlayerData targetData) {
    targetData.setPremiumMatches(0);
    targetData.setPremiumElo(1000);
    player.sendMessage(CC.color(
        "&a" + target.getName() + "'s premium matches and ELO have been successfully reset."));
  }

  @Subcommand("add")
  public void addPlayerPremiumMatches(Player player, Player target, PracticePlayerData targetData,
      int amount) {
    targetData.setPremiumMatches(targetData.getPremiumMatches() + amount);
    player.sendMessage(
        CC.color("&aYou have given " + amount + " Premium Matches to " + target.getName()));
    target.sendMessage(
        CC.color("&aYou have been given " + amount + " Premium Matches by " + player.getName()));
  }

  @Subcommand("remove")
  public void removePlayerPremiumMatches(Player player, Player target,
      PracticePlayerData targetData, int amount) {
    targetData.setPremiumMatches(targetData.getPremiumMatches() - amount);
    player.sendMessage(
        CC.color("&cYou have taken " + amount + " Premium Matches from " + target.getName()));
    target.sendMessage(
        CC.color("&c" + amount + " Premium Matches have been removed from your profile."));
  }
}
