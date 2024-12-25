package dev.demeng.frost.commands.user;

import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.tournament.Tournament;
import dev.demeng.frost.game.tournament.TournamentState;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.host.settings.EventSettingsMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.Clickable;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TournamentCommand extends Command {

  private final Frost plugin = Frost.getInstance();

  public TournamentCommand() {
    super("tournament");
    this.setUsage(ChatColor.RED + "Usage: /tournament [args]");
  }

  @Override
  public boolean execute(CommandSender sender, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }

    Player player = (Player) sender;
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (args.length == 0) {
      if (player.hasPermission("frost.host.tournament")) {
        for (String list : plugin.getMessagesConfig().getConfig()
            .getStringList("MESSAGES.TOURNAMENT.ADMIN-HELP")) {
          sendMessage(player, list);
        }
      } else {
        for (String list : plugin.getMessagesConfig().getConfig()
            .getStringList("MESSAGES.TOURNAMENT.USER-HELP")) {
          sendMessage(player, list);
        }
      }

      return true;
    }

    if (player.hasPermission("frost.host.tournament")) {
      switch (args[0].toLowerCase()) {
        case "host":
          if (plugin.getManagerHandler().getTournamentManager().getTournaments().size() == 0) {
            new EventSettingsMenu("tournament", "none").openMenu(player);
          } else {
            player.sendMessage(ChatColor.RED + "There is an ongoing tournament currently.");
          }
          break;
        case "stop":
          if (args.length == 1) {
            Tournament tournament = plugin.getManagerHandler().getTournamentManager()
                .getTournament();
            if (tournament != null) {
              try {
                tournament.getPlayers().forEach(uuid -> {
                  Player tournamentPlayer = plugin.getServer().getPlayer(uuid);
                  if (tournamentPlayer != null) {
                    plugin.getManagerHandler().getTournamentManager().leaveTournament(player);
                  }
                });
              } catch (Exception ignored) {
              }

              tournament.getPlayers().clear();
              tournament.getMatches().clear();

              plugin.getManagerHandler().getTournamentManager().removeTournament();
              player.sendMessage(ChatColor.RED + "Successfully stopped tournament!");
            } else {
              player.sendMessage(ChatColor.RED + "This tournament does not exist.");
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /tournament stop");
          }
          break;
        case "alert":
          if (args.length == 1) {
            Tournament tournament = plugin.getManagerHandler().getTournamentManager()
                .getTournament();
            if (tournament != null) {
              for (String message : CC.color(Frost.getInstance().getMessagesConfig().getConfig()
                  .getStringList("MESSAGES.TOURNAMENT.ANNOUNCEMENT"))) {
                Clickable clickable = new Clickable(message
                    .replace("<host>", tournament.getHost().getName())
                    .replace("<kit>", tournament.getKitName())
                    .replace("<players>", String.valueOf(tournament.getPlayers().size()))
                    .replace("<maxPlayers>", String.valueOf(tournament.getSize()))
                    .replace("<teamSize>", String.valueOf(tournament.getTeamSize())),
                    CC.color(plugin.getMessagesConfig().getConfig()
                        .getString("MESSAGES.TOURNAMENT.ANNOUNCEMENT-CLICKABLE")),
                    "/tournament join");

                Bukkit.getServer().getOnlinePlayers()
                    .stream().filter(tournamentPlayer -> !tournament.getPlayers()
                        .contains(tournamentPlayer.getUniqueId()))
                    .collect(Collectors.toList()).forEach(clickable::sendToPlayer);
              }
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /tournament alert");
          }
          break;
        case "forcestart":
          if (args.length == 1) {
            Tournament tournament = plugin.getManagerHandler().getTournamentManager()
                .getTournament();
            if (tournament != null) {
              if (tournament.getTournamentState() == TournamentState.WAITING) {
                tournament.setCountdown(11);
                tournament.setTournamentState(TournamentState.STARTING);
                for (String info : plugin.getMessagesConfig().getConfig()
                    .getStringList("MESSAGES.TOURNAMENT.FORCE-STARTED")) {
                  Bukkit.getServer().broadcastMessage(CC.color(info)
                      .replace("<kit>", tournament.getKitName())
                      .replace("<teamSize>", String.valueOf(tournament.getTeamSize()))
                      .replace("<players>", String.valueOf(tournament.getPlayers().size()))
                      .replace("<maxPlayers>", String.valueOf(tournament.getSize()))
                  );
                }
              } else if (tournament.getTournamentState() == TournamentState.STARTING
                  || tournament.getTournamentState() == TournamentState.FIGHTING) {
                player.sendMessage(CC.color(
                    "&cYou cannot force-start the tournament because it has already started!"));
              }
            } else {
              player.sendMessage(CC.color("&cThere are no available tournaments."));
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /tournament forcestart");
          }
          break;
        case "join":
          if (joinTournamentArg(args, player, practicePlayerData)) {
            return true;
          }
          break;
        case "leave":
          if (args.length == 1) {
            boolean inTournament = plugin.getManagerHandler().getTournamentManager()
                .isInTournament(player.getUniqueId());
            if (inTournament) {
              this.leaveTournament(player);
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /tournament leave");
          }
          break;
        case "status":
          if (statusTournamentArg(args, player)) {
            return true;
          }
          break;
        default:
          if (player.hasPermission("frost.host.tournament")) {
            for (String list : plugin.getMessagesConfig().getConfig()
                .getStringList("MESSAGES.TOURNAMENT.ADMIN-HELP")) {
              sendMessage(player, list);
            }
          } else {
            for (String list : plugin.getMessagesConfig().getConfig()
                .getStringList("MESSAGES.TOURNAMENT.USER-HELP")) {
              sendMessage(player, list);
            }
          }
          break;
      }
    } else {
      switch (args[0].toLowerCase()) {
        case "join":
          if (joinTournamentArg(args, player, practicePlayerData)) {
            return true;
          }
          break;
        case "leave":
          if (args.length == 1) {
            boolean inTournament = plugin.getManagerHandler().getTournamentManager()
                .isInTournament(player.getUniqueId());
            if (inTournament) {
              this.leaveTournament(player);
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /tournament leave");
          }
          break;
        case "status":
          if (statusTournamentArg(args, player)) {
            return true;
          }
          break;
        default:
          if (player.hasPermission("frost.host.tournament")) {
            for (String list : plugin.getMessagesConfig().getConfig()
                .getStringList("MESSAGES.TOURNAMENT.ADMIN-HELP")) {
              sendMessage(player, list);
            }
          } else {
            for (String list : plugin.getMessagesConfig().getConfig()
                .getStringList("MESSAGES.TOURNAMENT.USER-HELP")) {
              sendMessage(player, list);
            }
          }
          break;
      }
    }

    return false;
  }

  private boolean statusTournamentArg(String[] args, Player player) {
    if (args.length == 1) {
      if (plugin.getManagerHandler().getTournamentManager().getTournaments().size() == 0) {
        player.sendMessage(ChatColor.RED + "There is no available tournaments.");
        return true;
      }

      for (Tournament tournament : plugin.getManagerHandler().getTournamentManager()
          .getTournaments().values()) {
        if (tournament == null) {
          player.sendMessage(ChatColor.RED + "This tournament doesn't exist.");
          return true;
        }

        player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH
            + "----------------------------------------------------");
        player.sendMessage(" ");
        player.sendMessage(
            ChatColor.AQUA + "Tournament (" + tournament.getTeamSize() + "v"
                + tournament.getTeamSize() + ") " + ChatColor.WHITE
                + tournament.getKitName());

        if (tournament.getMatches().size() == 0) {
          player.sendMessage(ChatColor.RED + "There are no available tournament matches.");
          player.sendMessage(" ");
          player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH
              + "----------------------------------------------------");
          return true;
        }

        for (UUID matchUUID : tournament.getMatches()) {
          Match match = plugin.getManagerHandler().getMatchManager().getMatchFromUUID(matchUUID);

          MatchTeam teamA = match.getTeams().get(0);
          MatchTeam teamB = match.getTeams().get(1);

          boolean teamAParty = teamA.getAlivePlayers().size() > 1;
          boolean teamBParty = teamB.getAlivePlayers().size() > 1;
          String teamANames = (teamAParty ? teamA.getLeaderName() + "'s Party"
              : teamA.getLeaderName());
          String teamBNames = (teamBParty ? teamB.getLeaderName() + "'s Party"
              : teamB.getLeaderName());

          Clickable clickable = new Clickable(CC.color(
              "&f» &a" + teamANames + "&7 vs &c" + teamBNames + " &f« &a[Click to Spectate]"),
              ChatColor.GRAY + "Click to spectate",
              "/spectate " + teamA.getLeaderName());

          clickable.sendToPlayer(player);
        }

        player.sendMessage(" ");
        player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH
            + "----------------------------------------------------");
      }
    } else {
      player.sendMessage(ChatColor.RED + "Usage: /tournament status");
    }

    return false;
  }

  private boolean joinTournamentArg(String[] args, Player player,
      PracticePlayerData practicePlayerData) {
    if (args.length == 1) {
      if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
        sendMessage(player, plugin.getMessagesConfig().getConfig()
            .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
        return true;
      }

      if (plugin.getManagerHandler().getTournamentManager().isInTournament(player.getUniqueId())) {
        player.sendMessage(ChatColor.RED + "You are currently in a tournament.");
        return true;
      }

      Tournament tournament = plugin.getManagerHandler().getTournamentManager().getTournament();
      if (tournament != null) {
        if (tournament.isTeamTournament()) {
          if (plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId()) == null) {
            player.sendMessage(
                ChatColor.RED + "The party size must be of " + tournament.getTeamSize()
                    + " players.");
            return true;
          }
        }

        Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
        if (party != null && party.getMembers().size() != tournament.getTeamSize()) {
          player.sendMessage(ChatColor.RED + "The party size must be of " + tournament.getTeamSize()
              + " players.");
          return true;
        }

        if (tournament.getSize() > tournament.getPlayers().size()) {
          if ((tournament.getTournamentState() == TournamentState.WAITING
              || tournament.getTournamentState() == TournamentState.STARTING)
              && tournament.getCurrentRound() == 1) {
            plugin.getManagerHandler().getTournamentManager().joinTournament(0, player);
          } else {
            player.sendMessage(ChatColor.RED + "Sorry! The tournament already started.");
          }
        } else {
          player.sendMessage(ChatColor.RED + "Sorry! The tournament is already full.");
        }
      } else {
        player.sendMessage(ChatColor.RED + "There are no tournaments being hosted currently!");
      }
    } else {
      player.sendMessage(ChatColor.RED + "Usage: /tournament join");
    }

    return false;
  }

  private void leaveTournament(Player player) {
    Tournament tournament = plugin.getManagerHandler().getTournamentManager()
        .getTournament(player.getUniqueId());
    if (tournament != null) {
      plugin.getManagerHandler().getTournamentManager().leaveTournament(player);
    }
  }
}
