package dev.demeng.frost.runnable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.game.tournament.Tournament;
import dev.demeng.frost.game.tournament.TournamentState;
import dev.demeng.frost.game.tournament.TournamentTeam;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class TournamentRunnable extends BukkitRunnable {

  private final Frost plugin = Frost.getInstance();
  private final Tournament tournament;

  @Override
  public void run() {
    if (!plugin.getManagerHandler().getTournamentManager().getTournaments().isEmpty()) {
      if (this.tournament.getTournamentState() == TournamentState.STARTING) {
        int countdown = this.tournament.decrementCountdown();
        if (countdown == 0) {
          if (this.tournament.getCurrentRound() == 1) {
            Set<UUID> players = Sets.newConcurrentHashSet(this.tournament.getPlayers());
            if (!tournament.isTeamTournament()) {
              List<UUID> currentTeam = null;
              for (UUID player : players) {
                if (currentTeam == null) {
                  currentTeam = new ArrayList<>();
                }
                currentTeam.add(player);
                if (currentTeam.size() == this.tournament.getTeamSize()) {
                  TournamentTeam team = new TournamentTeam(currentTeam.get(0), currentTeam);
                  this.tournament.addAliveTeam(team);
                  for (UUID teammate : team.getPlayers()) {
                    tournament.setPlayerTeam(teammate, team);
                  }
                  currentTeam = null;
                }
              }
            } else {
              for (UUID player : players) {
                if (plugin.getManagerHandler().getPartyManager().isLeader(player)) {
                  Party party = plugin.getManagerHandler().getPartyManager().getParty(player);
                  if (party != null) {
                    TournamentTeam team = new TournamentTeam(party.getLeader(),
                        Lists.newArrayList(party.getMembers()));
                    this.tournament.addAliveTeam(team);
                    for (UUID member : party.getMembers()) {
                      if (tournament.getPlayerTeam(member) == null) {
                        tournament.setPlayerTeam(member, team);
                      }
                    }
                  }
                }
              }
            }
          }

          List<TournamentTeam> teams = this.tournament.getAliveTeams();
          Collections.shuffle(teams);
          for (int i = 0; i < teams.size(); i += 2) {
            TournamentTeam teamA = teams.get(i);
            if (teams.size() > i + 1) {
              TournamentTeam teamB = teams.get(i + 1);
              for (UUID playerUUID : teamA.getAlivePlayers()) {
                this.removeSpectator(playerUUID);
              }
              for (UUID playerUUID : teamB.getAlivePlayers()) {
                this.removeSpectator(playerUUID);
              }

              MatchTeam matchTeamA = new MatchTeam(teamA.getLeader(),
                  new ArrayList<>(teamA.getAlivePlayers()), 0);
              MatchTeam matchTeamB = new MatchTeam(teamB.getLeader(),
                  new ArrayList<>(teamB.getAlivePlayers()), 1);

              Kit kit = plugin.getManagerHandler().getKitManager()
                  .getKit(this.tournament.getKitName());
              Match match = new Match(
                  plugin.getManagerHandler().getArenaManager().getRandomArena(kit), kit,
                  QueueType.UNRANKED, matchTeamA, matchTeamB);

              Player leaderA = plugin.getServer().getPlayer(teamA.getLeader());
              Player leaderB = plugin.getServer().getPlayer(teamB.getLeader());

              ConfigCursor configCursor = new ConfigCursor(Frost.getInstance().getMessagesConfig(),
                  "MESSAGES.TOURNAMENT");
              match.broadcast(configCursor.getString("STARTING-TOURNAMENT-MATCH")
                  .replace("<leaderA>", leaderA.getName())
                  .replace("<leaderB>", leaderB.getName())
              );

              plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getManagerHandler().getMatchManager().createMatch(match);
                this.tournament.addMatch(match.getMatchId());
                plugin.getManagerHandler().getTournamentManager()
                    .addTournamentMatch(match.getMatchId());
              });
            } else {
              for (UUID uuid : teamA.getAlivePlayers()) {
                CC.sendMessage(plugin.getServer().getPlayer(uuid),
                    plugin.getMessagesConfig().getConfig()
                        .getString("MESSAGES.TOURNAMENT.ROUND-SKIPPED"));
              }
            }
          }

          for (String message : Frost.getInstance().getMessagesConfig().getConfig()
              .getStringList("MESSAGES.TOURNAMENT.STARTED")) {
            tournament.broadcast(message
                .replace("<teamSize>", String.valueOf(tournament.getTeamSize()))
                .replace("<kit>", tournament.getKitName())
                .replace("<round>", String.valueOf(tournament.getCurrentRound()))
                .replace("<players>", String.valueOf(tournament.getPlayers().size()))
            );
          }
          this.tournament.setTournamentState(TournamentState.FIGHTING);
        } else if ((countdown % 5 == 0 || countdown < 5) && countdown > 0) {
          String announce = plugin.getMessagesConfig().getConfig()
              .getString("MESSAGES.TOURNAMENT.COUNTDOWN")
              .replace("<round>", String.valueOf(this.tournament.getCurrentRound()))
              .replace("<time>", String.valueOf(countdown)
              );

          this.tournament.broadcastWithSound(announce, Sound.NOTE_PLING);
        }
      }
    } else {
      this.cancel();
    }
  }

  private void removeSpectator(UUID playerUUID) {
    Player player = plugin.getServer().getPlayer(playerUUID);
    if (player != null) {
      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (practicePlayerData.getPlayerState() == PlayerState.SPECTATING) {
        plugin.getManagerHandler().getMatchManager().removeSpectator(player);
        if (!practicePlayerData.getCachedPlayer().isEmpty()) {
          practicePlayerData.getCachedPlayer().clear();
        }
      }
    }
  }
}
