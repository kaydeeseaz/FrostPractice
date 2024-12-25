package dev.demeng.frost.commands.admin.arena;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("arena set")
@CommandPermission("frost.admin")
public class ArenaSetCommand {

  @Dependency private Frost plugin;
  private static final String NO_ARENA = CC.color("&4&lERROR&4! &cThat arena doesn't exist!");

  @DefaultFor("arena set")
  public void getHelpMessage(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()
        + " &8- &fArena Set - Command Help"));
    player.sendMessage(CC.color("&a&l • &7 - &fRequired command"));
    player.sendMessage(CC.color("&e&l • &7 - &fOptional command"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&e • &b/arena set icon <arena>"));
    player.sendMessage(CC.color("&7&oSets the arena icon"));
    player.sendMessage(" ");
    player.sendMessage(CC.color("&a • &b/arena set a <arena>"));
    player.sendMessage(CC.color("&7&o1st player spawn (blue-team|id=1)"));
    player.sendMessage(" ");
    player.sendMessage(CC.color("&a • &b/arena set b <arena>"));
    player.sendMessage(CC.color("&7&o2nd player spawn (red-team|id=0)"));
    player.sendMessage(" ");
    player.sendMessage(CC.color("&a • &b/arena set min <arena>"));
    player.sendMessage(CC.color("&7&oLowest corner position of the arena"));
    player.sendMessage(" ");
    player.sendMessage(CC.color("&a • &b/arena set max <arena>"));
    player.sendMessage(CC.color("&7&oHighest corner position of the arena"));
    player.sendMessage(" ");
    player.sendMessage(CC.color("&a • &b/arena set buildmax <arena>"));
    player.sendMessage(CC.color("&7&oHighest build height of arena"));
    player.sendMessage(" ");
    player.sendMessage(CC.color("&a • &b/arena set deadzone <arena>"));
    player.sendMessage(CC.color("&7&oLowest player alive pos of arena"));
    player.sendMessage(" ");
    player.sendMessage(CC.color("&e • &b/arena set portalprot <arena> <radius>"));
    player.sendMessage(CC.color("&7&oPortal protection radius of arena"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("icon")
  public void setIcon(Player player, Arena arena) {
    if (arena != null) {
      if (player.getItemInHand().getType() != Material.AIR) {
        arena.setIcon(player.getItemInHand().getType().name());
        arena.setIconData(player.getItemInHand().getDurability());
        player.sendMessage(
            CC.color("&aSuccessfully set icon for arena '&l" + arena.getName() + "&a'!"));
      } else {
        player.sendMessage(CC.color("&cYou must be holding an item to set the arena icon!"));
      }
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("a")
  public void setA(Player player, Arena arena) {
    if (arena != null) {
      arena.setA(CustomLocation.fromBukkitLocation(player.getLocation()));
      player.sendMessage(
          CC.color("&aSuccessfully set position A for arena '&l" + arena.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("b")
  public void setB(Player player, Arena arena) {
    if (arena != null) {
      arena.setB(CustomLocation.fromBukkitLocation(player.getLocation()));
      player.sendMessage(
          CC.color("&aSuccessfully set position B for arena '&l" + arena.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("min")
  public void setMin(Player player, Arena arena) {
    if (arena != null) {
      arena.setMin(CustomLocation.fromBukkitLocation(player.getLocation()));
      player.sendMessage(CC.color(
          "&aSuccessfully set lowest corner position for arena '&l" + arena.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("max")
  public void setMax(Player player, Arena arena) {
    if (arena != null) {
      arena.setMax(CustomLocation.fromBukkitLocation(player.getLocation()));
      player.sendMessage(CC.color(
          "&aSuccessfully set highest corner position for arena '&l" + arena.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("buildmax")
  public void setBuildMax(Player player, Arena arena) {
    if (arena != null) {
      arena.setBuildMax(player.getLocation().getBlockY());
      player.sendMessage(CC.color(
          "&aSuccessfully set maximum buildable height position for arena '&l" + arena.getName()
              + "&a'!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("deadzone")
  public void setDeadzone(Player player, Arena arena) {
    if (arena != null) {
      arena.setDeadZone(player.getLocation().getBlockY());
      player.sendMessage(CC.color(
          "&aSuccessfully set deadzone location for arena '&l" + arena.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("portalprot")
  public void setPortalProtectionRadius(Player player, Arena arena, int radius) {
    if (arena != null) {
      arena.setPortalProt(radius);
      player.sendMessage(CC.color(
          "&aSuccessfully set portal protection radius for arena '&l" + arena.getName()
              + "&a'! &ato &l" + radius + "&a!"));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }
}
