package dev.demeng.frost.commands.admin.arena;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("chunk")
@CommandPermission("frost.admin")
public class ArenaChunkCommands {

  @Dependency private Frost plugin;
  private static final String NO_ARENA = CC.color("&4&lERROR&4! &cThat arena doesn't exist!");

  @DefaultFor("chunk")
  public void getHelpMessage(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()
        + " &8- &fChunks - Command Help"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&7 • &b/chunk save <arenaName> <copyNumber>"));
    player.sendMessage(CC.color("&7 • &b/chunk reset <arenaName> <copyNumber>"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("save")
  public void saveArenaChunk(Player player, Arena arena, int copyNumber) {
    if (arena != null) {
      if (arena.getStandaloneArenas().get(copyNumber) == null) {
        player.sendMessage(CC.color("&aThat copy doesn't exist!"));
        return;
      }

      ChunkRestorationManager.getIChunkRestoration()
          .copy(arena.getStandaloneArenas().get(copyNumber));
      player.sendMessage(CC.color(
          "&aSuccessfully saved standalone arena chunks for '&l" + arena.getName() + "&a' #"
              + copyNumber + "."));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }

  @Subcommand("reset")
  public void resetArenaChunk(Player player, Arena arena, int copyNumber) {
    if (arena != null) {
      if (arena.getStandaloneArenas().get(copyNumber) == null) {
        player.sendMessage(CC.color("&aThat copy doesn't exist!"));
        return;
      }

      ChunkRestorationManager.getIChunkRestoration()
          .reset(arena.getStandaloneArenas().get(copyNumber));
      player.sendMessage(CC.color(
          "&aSuccessfully reset standalone arena chunks for '&l" + arena.getName() + "&a' #"
              + copyNumber + "."));
    } else {
      player.sendMessage(NO_ARENA);
    }
  }
}
