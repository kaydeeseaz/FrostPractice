package dev.demeng.frost.commands.admin.stats;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class PlayerDebugCommand {

  @Dependency private Frost plugin;

  @Command("debugplayer")
  @CommandPermission("frost.admin")
  public void getPlayerDebugInformation(Player player, @Default("me") Player target) {
    PracticePlayerData targetData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(target.getUniqueId());
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lPlayer State Debug"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color(" &9&l▸ &fName: &b" + target.getName()));
    player.sendMessage(CC.color(" &9&l▸ &fUUID: &b" + target.getUniqueId()));
    player.sendMessage(CC.color(" &9&l▸ &fNoDamageTicks: &b" + target.getNoDamageTicks()));
    player.sendMessage(
        CC.color(" &9&l▸ &fMaxNoDamageTicks: &b" + target.getMaximumNoDamageTicks()));
    player.sendMessage(CC.color(" &9&l▸ &fState: &b" + targetData.getPlayerState()));
    player.sendMessage(CC.color(" &9&l▸ &fWorld: &b" + target.getWorld().getName()));
    player.sendMessage(CC.color(" &9&l▸ &fisFollowing: &b" + targetData.isFollowing()));
    player.sendMessage(CC.color(" &9&l▸ &fisSpectating: &b" + targetData.isSpectating()));
    player.sendMessage(CC.color(" &9&l▸ &fisSilent: &b" + targetData.isSilent()));
    player.sendMessage(CC.color(
        " &9&l▸ &fPing Range: &b" + (targetData.getPlayerSettings().getPingRange() == -1
            ? "Unrestricted" : targetData.getPlayerSettings().getPingRange())));
    player.sendMessage(CC.color(" &9&l▸ &fLocation:"));
    player.sendMessage(CC.color("  &3 » &fX: &b" + Math.round(target.getLocation().getX())));
    player.sendMessage(CC.color("  &3 » &fY: &b" + Math.round(target.getLocation().getY())));
    player.sendMessage(CC.color("  &3 » &fZ: &b" + Math.round(target.getLocation().getZ())));
    player.sendMessage(CC.color(" "));
    if (!targetData.getCachedPlayer().isEmpty()) {
      player.sendMessage(CC.color(
          " &9&l▸ &fCached Player: &b" + targetData.getCachedPlayer().get(target).getName()));
    } else {
      player.sendMessage(
          CC.color(" &9&l▸ &fCached Player: &b" + targetData.getCachedPlayer().get(target)));
    }
    player.sendMessage(
        CC.color(" &9&l▸ &fCachePlayerMap values: &b" + targetData.getCachedPlayer().values()));
    player.sendMessage(CC.CHAT_BAR);
  }
}
