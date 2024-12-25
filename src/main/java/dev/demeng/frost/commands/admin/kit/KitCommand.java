package dev.demeng.frost.commands.admin.kit;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.CC;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("kit")
@CommandPermission("frost.admin")
public class KitCommand {

  @Dependency private Frost plugin;
  private final String NO_KIT = CC.color("&4&lERROR&4! &cThat kit doesn't exist!");

  @DefaultFor("kit")
  public void getHelp(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()));
    player.sendMessage(CC.color("&7Kit Subcommands: &f/kit <get|set|toggle>"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&7 • &b/kit list"));
    player.sendMessage(CC.color("&7 • &b/kit info <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit create <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit delete <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit whitelistArena <kitName> <arenaName>"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("create")
  public void createKit(Player player, String name) {
    Kit kit = plugin.getManagerHandler().getKitManager().getKit(name);
    if (kit == null) {
      plugin.getManagerHandler().getKitManager().createKit(name);
      player.sendMessage(CC.color("&aSuccessfully created kit " + name + "."));
    } else {
      player.sendMessage(CC.color("&cThat kit already exists!"));
    }
  }

  @Subcommand("delete")
  public void deleteKit(Player player, Kit kit) {
    if (kit != null) {
      plugin.getManagerHandler().getKitManager().deleteKit(kit.getName());
      player.sendMessage(CC.color("&aSuccessfully deleted kit " + kit.getName() + "."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("list")
  public void getKitsList(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color(
        "&b&lKits List &7(&f" + plugin.getManagerHandler().getKitManager().getKits().size()
            + "&b in total&7)"));
    player.sendMessage(CC.CHAT_BAR);
    for (Kit kits : plugin.getManagerHandler().getKitManager().getKits()) {
      player.sendMessage(CC.color(
          " &9&l▸ &b" + kits.getName() + " &8[" + (kits.isEnabled() ? "&aEnabled" : "&cDisabled")
              + "&8]"));
    }
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("info")
  public void getKitInfo(Player player, Kit kit) {
    if (kit != null) {
      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(CC.color("&b&lKit Information"));
      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(CC.color(" &9&l▸ &fName: &b" + kit.getName()));
      player.sendMessage(CC.color(" &9&l▸ &fDisplayName: &b" + kit.getDisplayName()));
      player.sendMessage(
          CC.color(" &9&l▸ &fState: " + (kit.isEnabled() ? "&aEnabled" : "&cDisabled")));
      player.sendMessage(
          CC.color(" &9&l▸ &fRanked: &b" + (kit.isRanked() ? "&aEnabled" : "&cDisabled")));
      player.sendMessage(CC.color(" &9&l▸ &fKB Profile: &b" + kit.getKbProfile()));
      player.sendMessage(CC.color(" &9&l▸ &fUnranked Position: &b" + kit.getUnrankedPos()));
      player.sendMessage(CC.color(" &9&l▸ &fRanked Position: &b" + kit.getRankedPos()));
      player.sendMessage(CC.color(" &9&l▸ &fEditor Position: &b" + kit.getEditorPos()));
      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(
          CC.color(" &9&l▸ &fWhitelisted Arenas: &b" + kit.getArenaWhiteList().size()));
      player.sendMessage(CC.color(
          "  &3 » &b" + StringUtils.join(Collections.singletonList(kit.getArenaWhiteList()),
              "&f, &b")));
      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(CC.color(" &9&l▸ &fKit Modes"));
      player.sendMessage(
          CC.color("  &3 » &fBattleRush: " + (kit.isBattleRush() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fBedWars: " + (kit.isBedWars() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fBuild: " + (kit.isBuild() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fBoxing: " + (kit.isBoxing() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fBridges: " + (kit.isBridges() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fCombo: " + (kit.isCombo() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fMLGRush: " + (kit.isMlgRush() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fNoFall: " + (kit.isNoFall() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fNoRegen: " + (kit.isNoRegen() ? "&atrue" : "&cfalse")));
      player.sendMessage(
          CC.color("  &3 » &fNoHunger: " + (kit.isNoHunger() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fSkyWars: " + (kit.isSkyWars() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fSpleef: " + (kit.isSpleef() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fSumo: " + (kit.isSumo() ? "&atrue" : "&cfalse")));
      player.sendMessage(
          CC.color("  &3 » &fStickFight: " + (kit.isStickFight() ? "&atrue" : "&cfalse")));
      player.sendMessage(
          CC.color("  &3 » &fShowHealth: " + (kit.isShowHealth() ? "&atrue" : "&cfalse")));
      player.sendMessage(
          CC.color("  &3 » &fSpawn FFA: " + (kit.isAllowSpawnFfa() ? "&atrue" : "&cfalse")));
      player.sendMessage(CC.color("  &3 » &fDamageTicks: &b" + kit.getDamageTicks()));
      player.sendMessage(CC.color("  &3 » &fLives: &b" + kit.getLives()));
      player.sendMessage(CC.CHAT_BAR);
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("whitelistarena")
  public void performArenaWhitelisting(Player player, Kit kit, String arenaName) {
    if (kit != null) {
      Arena arena = plugin.getManagerHandler().getArenaManager().getArena(arenaName);
      if (arena != null) {
        kit.whitelistArena(arena.getName());
        player.sendMessage(CC.color(kit.getArenaWhiteList().contains(arena.getName())
            ? "&aArena " + arena.getName() + " is now whitelisted to kit " + kit.getName() + "."
            : "&cArena " + arena.getName() + " is no longer whitelisted to kit " + kit.getName()
                + "."
        ));
      } else {
        player.sendMessage(CC.color("&4&lERROR&4! &cThat arena doesn't exist!"));
      }
    } else {
      player.sendMessage(NO_KIT);
    }
  }
}
