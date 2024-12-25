package dev.demeng.frost.game.tournament;

import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class Tournament {

  private final Frost plugin = Frost.getInstance();

  private final Set<UUID> players = new HashSet<>();
  private final Set<UUID> matches = new HashSet<>();
  private final List<TournamentTeam> aliveTeams = new ArrayList<>();
  private final Map<UUID, TournamentTeam> playerTeams = new ConcurrentHashMap<>();
  @Setter private Player host;
  private final int id;
  private final int teamSize;
  private final int size;
  private final String kitName;
  @Setter private TournamentState tournamentState = TournamentState.WAITING;
  @Setter private int currentRound = 1;
  @Setter private int countdown = 31;

  public MatchRandomTeam getRandomTeam() {
    List<TournamentTeam> teams = this.getAliveTeams();
    Collections.shuffle(teams);
    TournamentTeam teamA;
    TournamentTeam teamB;

    for (int i = 0; i < teams.size(); i += 2) {
      teamA = teams.get(i);
      if (teams.size() > i + 1) {
        teamB = teams.get(i + 1);
        return new MatchRandomTeam(teamA, teamB);
      } else {
        for (UUID uuid : teamA.getAlivePlayers()) {
          sendMessage(plugin.getServer().getPlayer(uuid), plugin.getMessagesConfig().getConfig()
              .getString("MESSAGES.TOURNAMENT.ROUND-SKIPPED"));
        }
      }
    }

    return null;
  }

  public void addPlayer(UUID uuid) {
    this.players.add(uuid);
  }

  public void addAliveTeam(TournamentTeam team) {
    this.aliveTeams.add(team);
  }

  public void killTeam(TournamentTeam team) {
    this.aliveTeams.remove(team);
  }

  public void setPlayerTeam(UUID uuid, TournamentTeam team) {
    this.playerTeams.put(uuid, team);
  }

  public TournamentTeam getPlayerTeam(UUID uuid) {
    return this.playerTeams.get(uuid);
  }

  public void removePlayer(UUID uuid) {
    this.players.remove(uuid);
  }

  public void addMatch(UUID uuid) {
    this.matches.add(uuid);
  }

  public void removeMatch(UUID uuid) {
    this.matches.remove(uuid);
  }

  public boolean isTeamTournament() {
    return this.teamSize > 1;
  }

  public void broadcast(String message) {
    for (UUID uuid : this.players) {
      sendMessage(plugin.getServer().getPlayer(uuid), message);
    }
  }

  public void broadcastWithSound(String message, Sound sound) {
    for (UUID uuid : this.players) {
      Player player = plugin.getServer().getPlayer(uuid);

      sendMessage(player, message);
      player.playSound(player.getLocation(), sound, 10, 1);
    }
  }

  public int decrementCountdown() {
    if (countdown <= 0) {
      return 0;
    }

    return --this.countdown;
  }

  @AllArgsConstructor
  @Getter
  public class MatchRandomTeam {

    TournamentTeam team1;
    TournamentTeam team2;
  }
}
