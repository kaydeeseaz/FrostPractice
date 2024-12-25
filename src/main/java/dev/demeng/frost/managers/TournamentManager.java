package dev.demeng.frost.managers;

import static dev.demeng.frost.util.CC.color;
import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.tournament.Tournament;
import dev.demeng.frost.game.tournament.TournamentState;
import dev.demeng.frost.game.tournament.TournamentTeam;
import dev.demeng.frost.runnable.TournamentRunnable;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.util.TeamUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TournamentManager {

  private final Frost plugin = Frost.getInstance();

  private final Map<UUID, Integer> tournamentPlayers = new HashMap<>();
  private final Map<UUID, Integer> tournamentMatches = new HashMap<>();
  private final Map<Integer, Tournament> tournaments = new HashMap<>();

  private final ConfigCursor tMsg = new ConfigCursor(Frost.getInstance().getMessagesConfig(),
      "MESSAGES.TOURNAMENT");
  private final ConfigCursor tPlayerMsg = new ConfigCursor(Frost.getInstance().getMessagesConfig(),
      "ERROR-MESSAGES.PLAYER");

  public boolean isInTournament(UUID uuid) {
    return this.tournamentPlayers.containsKey(uuid);
  }

  public Tournament getTournament(UUID uuid) {
    Integer id = this.tournamentPlayers.get(uuid);
    if (id == null) {
      return null;
    }

    return this.tournaments.get(id);
  }

  public Tournament getTournamentFromMatch(UUID uuid) {
    Integer id = this.tournamentMatches.get(uuid);
    if (id == null) {
      return null;
    }

    return this.tournaments.get(id);
  }

  public void createTournament(CommandSender sender, int teamSize, int size, String kitName) {
    Tournament tournament = new Tournament(0, teamSize, size, kitName);
    this.tournaments.put(0, tournament);
    new TournamentRunnable(tournament).runTaskTimerAsynchronously(plugin, 20L, 20L);

    sender.sendMessage(
        ChatColor.GREEN + "Successfully created " + teamSize + "v" + teamSize + " " + kitName
            + " tournament. (Max players: " + size + ")");

    if (sender instanceof Player) {
      Player player = (Player) sender;
      tournament.setHost(player);
      new BukkitRunnable() {
        @Override
        public void run() {
          if (tournament.getCountdown() >= 5
              && tournament.getTournamentState() == TournamentState.WAITING) {
            player.performCommand("tournament alert");
          }
        }
      }.runTaskTimer(plugin, 0L, 300L);
    }
  }

  private void playerLeft(Tournament tournament, Player player) {
    TournamentTeam team = tournament.getPlayerTeam(player.getUniqueId());
    tournament.removePlayer(player.getUniqueId());
    sendMessage(player, tPlayerMsg.getString("LEFT-TOURNAMENT"));

    this.tournamentPlayers.remove(player.getUniqueId());
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);

    tournament.broadcast(tMsg.getString("PLAYER-LEFT")
        .replace("<player>", player.getName())
        .replace("<players>", String.valueOf(tournament.getPlayers().size()))
        .replace("<maxPlayers>", String.valueOf(tournament.getSize()))
    );

    if (team != null) {
      team.killPlayer(player.getUniqueId());
      if (team.getAlivePlayers().size() == 0) {
        tournament.killTeam(team);
        if (tournament.getAliveTeams().size() == 1) {
          TournamentTeam tournamentTeam = tournament.getAliveTeams().get(0);
          String names = TeamUtil.getNames(tournamentTeam);
          Bukkit.broadcastMessage(color(tMsg.getString("WINNER"))
              .replace("<winners>", names)
              .replace("<kit>", tournament.getKitName())
              .replace("<teamSize>", String.valueOf(tournament.getTeamSize()))
          );

          for (UUID playerUUID : tournamentTeam.getAlivePlayers()) {
            this.tournamentPlayers.remove(playerUUID);
            Player tournamentPlayer = plugin.getServer().getPlayer(playerUUID);
            plugin.getManagerHandler().getPlayerManager()
                .resetPlayerOrSpawn(tournamentPlayer, true);
          }

          plugin.getManagerHandler().getTournamentManager().removeTournament();
        }
      } else if (team.getLeader().equals(player.getUniqueId())) {
        team.setLeader(team.getAlivePlayers().get(0));
      }
    }
  }

  private void teamEliminated(Tournament tournament, TournamentTeam winnerTeam,
      TournamentTeam losingTeam) {
    for (UUID playerUUID : losingTeam.getPlayers()) {
      Player player = plugin.getServer().getPlayer(playerUUID);
      if (player != null) {
        tournament.removePlayer(player.getUniqueId());
        this.tournamentPlayers.remove(player.getUniqueId());
        sendMessage(player, tMsg.getString("ELIMINATED"));
      }
    }

    String soloAnnounce = tMsg.getString("PLAYER-ELIMINATED")
        .replace("<playerA>", losingTeam.getLeaderName())
        .replace("<playerB>", winnerTeam.getLeaderName());

    String teamAnnounce = tMsg.getString("TEAM-ELIMINATED")
        .replace("<playerA>", losingTeam.getLeaderName())
        .replace("<playerB>", winnerTeam.getLeaderName());

    String alive = tMsg.getString("REMAINING")
        .replace("<players>", String.valueOf(tournament.getPlayers().size()))
        .replace("<maxPlayers>", String.valueOf(tournament.getSize()));

    if (!tournament.isTeamTournament()) {
      tournament.broadcast(soloAnnounce);
    } else {
      tournament.broadcast(teamAnnounce);
    }

    tournament.broadcast(alive);
  }

  public void leaveTournament(Player player) {
    Tournament tournament = this.getTournament(player.getUniqueId());
    if (tournament == null) {
      return;
    }

    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party != null && tournament.getTournamentState() != TournamentState.FIGHTING) {
      if (plugin.getManagerHandler().getPartyManager().isLeader(player.getUniqueId())) {
        for (UUID memberUUID : party.getMembers()) {
          Player member = plugin.getServer().getPlayer(memberUUID);
          this.playerLeft(tournament, member);
        }
      } else {
        sendMessage(player, tPlayerMsg.getString("NOT-PARTY-LEADER"));
      }
    } else {
      this.playerLeft(tournament, player);
    }
  }

  private void playerJoined(Tournament tournament, Player player) {
    tournament.addPlayer(player.getUniqueId());

    this.tournamentPlayers.put(player.getUniqueId(), tournament.getId());
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);

    tournament.broadcast(tMsg.getString("PLAYER-JOINED")
        .replace("<player>", player.getName())
        .replace("<players>", String.valueOf(tournament.getPlayers().size()))
        .replace("<maxPlayers>", String.valueOf(tournament.getSize()))
    );
  }

  public void joinTournament(Integer id, Player player) {
    Tournament tournament = this.tournaments.get(id);
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party != null) {
      if (plugin.getManagerHandler().getPartyManager().isLeader(player.getUniqueId())) {
        if ((party.getMembers().size() + tournament.getPlayers().size()) <= tournament.getSize()) {
          if (party.getMembers().size() != tournament.getTeamSize()
              || party.getMembers().size() == 1) {
            player.sendMessage(
                ChatColor.RED + "The party size must be of " + tournament.getTeamSize()
                    + " players.");
          } else {
            for (UUID memberUUID : party.getMembers()) {
              Player member = plugin.getServer().getPlayer(memberUUID);
              this.playerJoined(tournament, member);
            }
          }
        } else {
          player.sendMessage(ChatColor.RED + "Sorry! The tournament is already full.");
        }
      } else {
        sendMessage(player, tPlayerMsg.getString("NOT-PARTY-LEADER"));
      }
    } else {
      this.playerJoined(tournament, player);
    }

    if (tournament.getPlayers().size() == tournament.getSize()) {
      tournament.setTournamentState(TournamentState.STARTING);
    }
  }

  public Tournament getTournament() {
    return this.tournaments.get(0);
  }

  public void removeTournament() {
    Tournament tournament = this.tournaments.get(0);
    if (tournament == null) {
      return;
    }

    this.tournaments.remove(0);
  }

  public void addTournamentMatch(UUID matchId) {
    this.tournamentMatches.put(matchId, 0);
  }

  public void removeTournamentMatch(Match match) {
    Tournament tournament = this.getTournamentFromMatch(match.getMatchId());
    if (tournament == null) {
      return;
    }

    tournament.removeMatch(match.getMatchId());
    this.tournamentMatches.remove(match.getMatchId());

    MatchTeam losingTeam =
        match.getWinningTeamId() == 0 ? match.getTeams().get(1) : match.getTeams().get(0);
    TournamentTeam losingTournamentTeam = tournament.getPlayerTeam(losingTeam.getLeader());

    MatchTeam winningTeam = match.getTeams().get(match.getWinningTeamId());
    TournamentTeam winningTournamentTeam = tournament.getPlayerTeam(winningTeam.getLeader());

    if (losingTournamentTeam != null) {
      tournament.killTeam(losingTournamentTeam);
      this.teamEliminated(tournament, winningTournamentTeam, losingTournamentTeam);
    }

    if (tournament.getMatches().size() == 0) {
      if (tournament.getAliveTeams().size() > 1) {
        tournament.setTournamentState(TournamentState.STARTING);
        tournament.setCurrentRound(tournament.getCurrentRound() + 1);
        tournament.setCountdown(11);
      } else {
        String names = TeamUtil.getNames(winningTournamentTeam);
        Bukkit.broadcastMessage(color(tMsg.getString("WINNER"))
            .replace("<winners>", names)
            .replace("<kit>", tournament.getKitName())
            .replace("<teamSize>", String.valueOf(tournament.getTeamSize()))
        );

        for (UUID playerUUID : winningTournamentTeam.getAlivePlayers()) {
          this.tournamentPlayers.remove(playerUUID);
          Player tournamentPlayer = plugin.getServer().getPlayer(playerUUID);
          plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(tournamentPlayer, true);
        }

        plugin.getManagerHandler().getTournamentManager().removeTournament();
      }
    }
  }

  public Map<Integer, Tournament> getTournaments() {
    return tournaments;
  }
}
