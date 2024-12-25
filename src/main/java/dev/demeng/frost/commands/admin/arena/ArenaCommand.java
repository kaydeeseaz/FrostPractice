package dev.demeng.frost.commands.admin.arena;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.runnable.ArenaCommandRunnable;
import dev.demeng.frost.runnable.ArenaCopyRemovalRunnable;
import dev.demeng.frost.user.ui.arena.ArenaManagerMenu;
import dev.demeng.frost.util.CC;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("arena")
@CommandPermission("frost.admin")
public class ArenaCommand {

  @Dependency private Frost plugin;
  private static final String NO_ARENA = CC.color("&4&lERROR&4! &cThat arena doesn't exist!");

  @DefaultFor("arena")
  public void getHelp(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()
        + " &8- &fArena - Command Help"));
    player.sendMessage(CC.color("&7Arena Subcommands: &f/arena set"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&7 • &b/arena list"));
    player.sendMessage(CC.color("&7 • &b/arena save"));
    player.sendMessage(CC.color("&7 • &b/arena manage"));
    player.sendMessage(CC.color("&7 • &b/arena info <arenaName>"));
    player.sendMessage(CC.color("&7 • &b/arena create <arenaName>"));
    player.sendMessage(CC.color("&7 • &b/arena delete <arenaName>"));
    player.sendMessage(CC.color("&7 • &b/arena toggle <arenaName>"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("create")
  public void createArena(Player player, String name) {
    Arena arena = plugin.getManagerHandler().getArenaManager().getArena(name);
    if (arena == null) {
      plugin.getManagerHandler().getArenaManager().createArena(name);
      player.sendMessage(CC.color("&a&lSUCCESS&a! &7Arena &b" + name + " &7has been created!"));
    } else {
      player.sendMessage(CC.color("&cThat arena already exists!"));
    }
  }

  @Subcommand("delete")
  public void deleteArena(Player player, String name) {
    Arena arena = plugin.getManagerHandler().getArenaManager().getArena(name);
    if (arena != null) {
      if (!plugin.getManagerHandler().getArenaManager()
          .getArena(name).getStandaloneArenas().isEmpty()) {
        int i = 0;
        for (StandaloneArena copy : plugin.getManagerHandler().getArenaManager().getArena(name)
            .getStandaloneArenas()) {
          new ArenaCopyRemovalRunnable(i, arena, copy).runTask(this.plugin);
          i++;
        }
      }

      plugin.getManagerHandler().getArenaManager().deleteArena(name);
      player.sendMessage(
          CC.color("&a&lSUCCESS&a! &7Arena &b" + arena.getName() + " &7has been deleted!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("toggle")
  public void toggleArenaAvailability(Player player, Arena arena) {
    if (arena != null) {
      arena.setEnabled(!arena.isEnabled());
      player.sendMessage(CC.color(arena.isEnabled()
          ? "&aSuccessfully enabled arena " + arena.getName() + "."
          : "&cSuccessfully disabled arena " + arena.getName() + "."
      ));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("list")
  public void getArenaList(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color(
        "&b&lArenas List &7(&f" + plugin.getManagerHandler().getArenaManager().getArenas().size()
            + "&b in total&7)"));
    player.sendMessage(CC.CHAT_BAR);
    for (Arena arena : plugin.getManagerHandler().getArenaManager().getArenas().values()) {
      player.sendMessage(CC.color(
          " &9&l▸ &b" + arena.getName() + " &8[" + (arena.isEnabled() ? "&aEnabled" : "&cDisabled")
              + "&8]"));
    }
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("info")
  public void getArenaInformation(Player player, Arena arena) {
    if (arena != null) {
      List<String> kitsWhitelisted = new ArrayList<>();
      for (Kit kit : plugin.getManagerHandler().getKitManager().getKits()) {
        if (kit.getArenaWhiteList().contains(arena.getName())) {
          kitsWhitelisted.add(kit.getName());
        }
      }

      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(CC.color("&b&lArena Information"));
      player.sendMessage(" ");
      player.sendMessage(CC.color(" &9&l▸ &fName: &b" + arena.getName()));
      player.sendMessage(
          CC.color(" &9&l▸ &fState: " + (arena.isEnabled() ? "&aEnabled" : "&cDisabled")));
      player.sendMessage(CC.color(" &9&l▸ &fBuildMax: " + arena.getBuildMax()));
      player.sendMessage(CC.color(" &9&l▸ &fDeadZone: " + arena.getDeadZone()));
      player.sendMessage(CC.color(" &9&l▸ &fPortalProt Radius: " + arena.getPortalProt()));
      player.sendMessage(CC.color(
          " &9&l▸ &f1st Spawn: &b" + Math.round(arena.getA().getX()) + "&7, &b" + Math.round(
              arena.getA().getY()) + "&7, &b" + Math.round(arena.getA().getZ())));
      player.sendMessage(CC.color(
          " &9&l▸ &f2nd Spawn: &b" + Math.round(arena.getB().getX()) + "&7, &b" + Math.round(
              arena.getB().getY()) + "&7, &b" + Math.round(arena.getB().getZ())));
      player.sendMessage(CC.color(
          " &9&l▸ &fMin Location: &b" + Math.round(arena.getMin().getX()) + "&7, &b" + Math.round(
              arena.getMin().getY()) + "&7, &b" + Math.round(arena.getMin().getZ())));
      player.sendMessage(CC.color(
          " &9&l▸ &fMax Location: &b" + Math.round(arena.getMax().getX()) + "&7, &b" + Math.round(
              arena.getMax().getY()) + "&7, &b" + Math.round(arena.getMax().getZ())));
      player.sendMessage(
          CC.color(" &9&l▸ &fStandalone Arenas: &b" + arena.getStandaloneArenas().size()));
      player.sendMessage(CC.color(
          " &9&l▸ &fAvailable Arenas: &b" + (arena.getAvailableArenas().isEmpty() ? +1
              : arena.getAvailableArenas().size())));
      player.sendMessage(" ");
      player.sendMessage(CC.color(" &9&l▸ &fKits with this Arena: &b" + kitsWhitelisted.size()));
      player.sendMessage(CC.color("  &3 » &b" + StringUtils.join(kitsWhitelisted, "&f, &b")));
      player.sendMessage(" ");
      player.sendMessage(CC.CHAT_BAR);
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("generate")
  public void generateArenaCopies(Player player, Arena arena, int copies) {
    plugin.getServer().getScheduler()
        .runTask(plugin, new ArenaCommandRunnable(plugin, arena, copies));
    plugin.getManagerHandler().getArenaManager().setGeneratingArenaRunnable(
        plugin.getManagerHandler().getArenaManager().getGeneratingArenaRunnable() + 1);
  }

  @Subcommand("manage")
  public void showArenaManagementMenu(Player player) {
    if (plugin.getManagerHandler().getArenaManager().getArenas().size() == 0) {
      player.sendMessage(CC.color("&cThere are no arenas."));
      return;
    }

    new ArenaManagerMenu().openMenu(player);
  }

  @Subcommand("save")
  public void saveArenas(Player player) {
    plugin.getManagerHandler().getArenaManager().reloadArenas();
    player.sendMessage(CC.color("&a&lSUCCESS&a! &7All arenas have been saved!"));
  }
}
