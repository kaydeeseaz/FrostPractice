package dev.demeng.frost.commands.admin;

import static dev.demeng.frost.util.CC.color;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.user.player.PracticePlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelMatchCommand extends Command {

  private final Frost plugin = Frost.getInstance();

  public CancelMatchCommand() {
    super("cancelmatch");
    this.setUsage(ChatColor.RED + "Usage: /cancelmatch <player>");
  }

  @Override
  public boolean execute(CommandSender sender, String label, String[] args) {
    if (!sender.hasPermission("frost.staff.cancelmatch")) {
      sender.sendMessage(color("&cNo permission."));
      return true;
    }

    Player player = (Player) sender;
    PracticePlayerData senderPlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (args.length == 0) {
      player.sendMessage(color("&cUsage: /cancelmatch <player>"));
      return true;
    }

    Player target = Bukkit.getPlayer(args[0]);
    Match match = plugin.getManagerHandler().getMatchManager().getMatch(target.getUniqueId());
    if (match == null) {
      player.sendMessage(color("&cThat player is not in a match"));
      return true;
    }

    PracticePlayerData targetPlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(target.getUniqueId());
    if (args.length == 1) {
      if (targetPlayerData == null) {
        player.sendMessage(color("&cPlayer not found!"));
        return true;
      }
      if (targetPlayerData.isInMatch()) {
        if (!match.isPartyMatch()) {
          match.setMatchState(MatchState.ENDING);
          match.getTeams().forEach(team -> team.alivePlayers().forEach(
              targets -> plugin.getManagerHandler().getPlayerManager()
                  .resetPlayerOrSpawn(targets, true)));
          match.spectatorPlayers()
              .forEach(plugin.getManagerHandler().getMatchManager()::removeSpectator);
          senderPlayerData.getCachedPlayer().clear();
          plugin.getManagerHandler().getMatchManager().removeMatch(match);
          player.sendMessage(
              color("&aSuccessfully cancelled &b" + target.getName() + "'s &amatch."));
          for (String message : plugin.getMessagesConfig().getConfig()
              .getStringList("MESSAGES.MATCH.CANCELLED")) {
            match.broadcast(message.replace("<cancelled_by>", sender.getName()));
          }
          if (match.getKit().isBuild() || match.getKit().isSpleef()) {
            ChunkRestorationManager.getIChunkRestoration().reset(match.getStandaloneArena());
            match.getArena().addAvailableArena(match.getStandaloneArena());
            plugin.getManagerHandler().getArenaManager()
                .removeArenaMatchUUID(match.getStandaloneArena());
          }
        } else {
          player.sendMessage(color("&cThis command can only be used on 1v1 matches!"));
        }
      } else {
        player.sendMessage(color("&c" + target.getName() + " is not in a match!"));
      }
    }

    return true;
  }
}
