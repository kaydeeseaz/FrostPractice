package dev.demeng.frost.providers.board;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.EventState;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.brackets.BracketsEvent;
import dev.demeng.frost.events.games.corners.FourCornersEvent;
import dev.demeng.frost.events.games.dropper.DropperEvent;
import dev.demeng.frost.events.games.gulag.GulagEvent;
import dev.demeng.frost.events.games.knockout.KnockoutEvent;
import dev.demeng.frost.events.games.lms.LMSEvent;
import dev.demeng.frost.events.games.oitc.OITCEvent;
import dev.demeng.frost.events.games.parkour.ParkourEvent;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.events.games.spleef.SpleefEvent;
import dev.demeng.frost.events.games.stoplight.StopLightEvent;
import dev.demeng.frost.events.games.sumo.SumoEvent;
import dev.demeng.frost.events.games.thimble.ThimbleEvent;
import dev.demeng.frost.events.games.tnttag.TNTTagEvent;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.match.listeners.SpecialMatchListener;
import dev.demeng.frost.game.queue.QueueEntry;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.scoreboard.scoreboard.BoardAdapter;
import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.timer.impl.EnderpearlTimer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardProvider implements BoardAdapter {

  private final Frost plugin = Frost.getInstance();

  private final ConfigCursor scoreboard = new ConfigCursor(
      Frost.getInstance().getScoreboardConfig(), "SCOREBOARD");
  private final ConfigCursor eventScoreboard = new ConfigCursor(
      Frost.getInstance().getEventScoreboardConfig(), "SCOREBOARD");
  private final ScoreboardReplacementProvider replacementProvider = new ScoreboardReplacementProvider();

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, scoreboard.getString("TITLE"));
  }

  @Override
  public List<String> getScoreboard(Player player, Board board, Set<BoardCooldown> cooldowns) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData == null) {
      plugin.getLogger().warning(
          player.getName() + "'s player data is null" + "(" + this.getClass().getName() + ")");
      return null;
    }

    if (!practicePlayerData.getPlayerSettings().isScoreboardToggled()) {
      return null;
    }

    switch (practicePlayerData.getPlayerState()) {
      case LOADING:
      case EDITING:
      case SPAWN:
      case EVENT:
      case SPECTATING:
      case QUEUE:
        return CC.parse(player, inLobbyScoreboard(player));
      case FIGHTING:
        return CC.parse(player, inMatchScoreboard(player));
      case FFA:
        return CC.parse(player, inFfaScoreboard(player));
    }

    return null;
  }

  private List<String> placeholderLines(String path, String state, Player player) {
    List<String> lines = new ArrayList<>();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    scoreboard.getStringList(path).forEach(text -> {
      switch (text) {
        case "<following>":
          if (practicePlayerData.isFollowing()) {
            scoreboard.getStringList("FOLLOWING").forEach(a -> lines.add(CC.parse(player, a)
                .replace("<FPL>",
                    Bukkit.getPlayer(practicePlayerData.getFollowingId()).getName())));
          }
          break;
        case "<silent>":
          if (practicePlayerData.isSilent()) {
            scoreboard.getStringList("SILENT").forEach(a -> lines.add(CC.parse(player, a)));
          }
          break;
        case "<ping_range>":
          if (practicePlayerData.isQueueing()) {
            Party party = plugin.getManagerHandler().getPartyManager()
                .getParty(player.getUniqueId());
            QueueEntry queueEntry = party == null ? plugin.getManagerHandler().getQueueManager()
                .getQueueEntry(player.getUniqueId())
                : plugin.getManagerHandler().getQueueManager().getQueueEntry(party.getLeader());
            if (queueEntry == null) {
              return;
            }

            int pingRange = PlayerUtil.getQueuePing(player, practicePlayerData);
            if (player.hasPermission("frost.vip.ping_range") && pingRange != -1) {
              for (String a : scoreboard.getStringList(
                  "RESTRICTED-PING" + (practicePlayerData.isTickedPing() ? "" : "-NOT-TICKED"))) {
                lines.add(CC.parse(player, a)
                    .replace("<cPingRange>",
                        String.valueOf(Math.max(practicePlayerData.getMinPing(), 0)))
                    .replace("<nPingRange>",
                        String.valueOf(Math.max(practicePlayerData.getMaxPing(), 0)))
                );
              }
            } else {
              for (String a : scoreboard.getStringList("UNRESTRICTED-PING")) {
                lines.add(CC.parse(player, a));
              }
            }
          }
          break;
        case "<isBridges>":
          Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
          if (match != null) {
            if (match.getKit().isBridges()) {
              MatchTeam blueTeam = match.getTeamById(1);
              MatchTeam redTeam = match.getTeamById(0);
              MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
              scoreboard.getStringList("BRIDGES").forEach(a -> lines.add(CC.parse(player, a)
                  .replace("<bGoal>", PlayerUtil.getBridgesScore(blueTeam.getBridgesPoints(), true))
                  .replace("<rGoal>", PlayerUtil.getBridgesScore(redTeam.getBridgesPoints(), false))
                  .replace("<goals>", String.valueOf(playerTeam.getBridgesPoints()))
                  .replace("<kills>", String.valueOf(
                      SpecialMatchListener.getPlayerKills().getOrDefault(player.getUniqueId(), 0))
                  ))
              );
            }
          }
          break;
        case "<isMlgRush>":
          Match mlgrush = plugin.getManagerHandler().getMatchManager()
              .getMatch(player.getUniqueId());
          if (mlgrush != null) {
            if (mlgrush.getKit().isMlgRush()) {
              MatchTeam blueTeam = mlgrush.getTeamById(1);
              MatchTeam redTeam = mlgrush.getTeamById(0);
              MatchTeam playerTeam = mlgrush.getTeams().get(practicePlayerData.getTeamId());
              scoreboard.getStringList("MLGRUSH").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bRed>", PlayerUtil.getBridgesScore(blueTeam.getBridgesPoints(), true))
                      .replace("<rBed>", PlayerUtil.getBridgesScore(redTeam.getBridgesPoints(), false))
                      .replace("<goals>", String.valueOf(playerTeam.getBridgesPoints()))
                  )
              );
            }
          }
          break;
        case "<isBedWars>":
          Match bedwars = plugin.getManagerHandler().getMatchManager()
              .getMatch(player.getUniqueId());
          if (bedwars != null) {
            if (bedwars.getKit().isBedWars()) {
              MatchTeam blueTeam = bedwars.getTeamById(1);
              MatchTeam redTeam = bedwars.getTeamById(0);
              scoreboard.getStringList("BEDWARS").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bBed>", PlayerUtil.getBedWarsScore(blueTeam.isHasBed(), true))
                      .replace("<rBed>", PlayerUtil.getBedWarsScore(redTeam.isHasBed(), false))
                  )
              );
            }
          }
          break;
        case "<isBattleRush>":
          Match battlerush = plugin.getManagerHandler().getMatchManager()
              .getMatch(player.getUniqueId());
          if (battlerush != null) {
            if (battlerush.getKit().isBattleRush()) {
              MatchTeam blueTeam = battlerush.getTeamById(1);
              MatchTeam redTeam = battlerush.getTeamById(0);
              MatchTeam playerTeam = battlerush.getTeams().get(practicePlayerData.getTeamId());
              scoreboard.getStringList("BATTLERUSH").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bGoal>",
                          PlayerUtil.getBattleRushScore(blueTeam.getBridgesPoints(), true))
                      .replace("<rGoal>",
                          PlayerUtil.getBattleRushScore(redTeam.getBridgesPoints(), false))
                      .replace("<goals>", String.valueOf(playerTeam.getBridgesPoints()))
                  )
              );
            }
          }
          break;
        case "<isStickFight>":
          Match stickfight = plugin.getManagerHandler().getMatchManager()
              .getMatch(player.getUniqueId());
          if (stickfight != null) {
            if (stickfight.getKit().isStickFight()) {
              MatchTeam blueTeam = stickfight.getTeamById(1);
              MatchTeam redTeam = stickfight.getTeamById(0);
              scoreboard.getStringList("STICKFIGHT").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bLives>", String.valueOf(blueTeam.getLives()))
                      .replace("<rLives>", String.valueOf(redTeam.getLives()))
                  )
              );
            }
          }
          break;
        case "<isBoxing>":
          Match boxingMatch = plugin.getManagerHandler().getMatchManager()
              .getMatch(player.getUniqueId());
          if (boxingMatch != null) {
            if (boxingMatch.getKit().isBoxing()) {
              MatchTeam playerTeam = boxingMatch.getTeams().get(practicePlayerData.getTeamId());
              MatchTeam enemyTeam =
                  playerTeam == boxingMatch.getTeams().get(0) ? boxingMatch.getTeams().get(1)
                      : boxingMatch.getTeams().get(0);
              PracticePlayerData opponentPlayerData = plugin.getManagerHandler().getPlayerManager()
                  .getPlayerData(enemyTeam.getPlayers().get(0));

              int yHits = practicePlayerData.getHits(), oHits = opponentPlayerData.getHits();
              int yCombo = practicePlayerData.getCombo(), oCombo = opponentPlayerData.getCombo();
              scoreboard.getStringList("BOXING").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<your_hits>", String.valueOf(practicePlayerData.getHits()))
                      .replace("<opponent_hits>", String.valueOf(opponentPlayerData.getHits()))
                      .replace("<hits>", yHits - oHits < oHits - yHits ? "&c(" + (yHits - oHits) + ")"
                          : "&a(+" + (yHits - oHits) + ")")
                      .replace("<combo>", (yCombo < 2 && oCombo < 2) ? "No Combo"
                          : yCombo > oCombo ? "&a" + yCombo + " Combo" : "&c" + oCombo + " Combo")
                  )
              );
            }
          }
          break;
        default:
          switch (state) {
            case "LOBBY":
              lines.add(replacementProvider.spawnReplace(text, player));
              break;
            case "PARTY":
              lines.add(replacementProvider.partySpawnReplace(text, player));
              break;
            case "PARTY-TOURNAMENT":
              lines.add(replacementProvider.partyTournamentSpawnReplace(text, player));
              break;
            case "TOURNAMENT":
              lines.add(replacementProvider.tournamentSpawnReplace(text, player));
              break;
            case "UNRANKED-QUEUE":
              lines.add(replacementProvider.unrankedQueueReplace(text, player));
              break;
            case "RANKED-QUEUE":
            case "PREMIUM-QUEUE":
              lines.add(replacementProvider.rankedQueueReplace(text, player));
              break;
            case "MATCH":
            case "MATCH-PING":
              lines.add(replacementProvider.matchReplace(text, player));
              break;
            case "PARTY-FFA":
            case "PARTY-SPLIT":
              lines.add(replacementProvider.partyMatchReplace(text, player));
              break;
          }
      }
    });

    return lines;
  }

  private List<String> placeholderLinesSpectating(String path, String state, Player player) {
    List<String> lines = new ArrayList<>();
    PracticePlayerData cachedPlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(
            plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
                .getCachedPlayer().get(player).getUniqueId());
    Match cachedMatch = plugin.getManagerHandler().getMatchManager()
        .getMatch(cachedPlayerData.getUniqueId());

    scoreboard.getStringList(path).forEach(text -> {
      switch (text) {
        case "<isBridges>":
          if (cachedMatch != null) {
            if (cachedMatch.getKit().isBridges()) {
              MatchTeam blueTeam = cachedMatch.getTeamById(1);
              MatchTeam redTeam = cachedMatch.getTeamById(0);
              scoreboard.getStringList("BRIDGES-SPEC").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bGoal>", PlayerUtil.getBridgesScore(blueTeam.getBridgesPoints(), true))
                      .replace("<rGoal>", PlayerUtil.getBridgesScore(redTeam.getBridgesPoints(), false))
                  )
              );
            }
          }
          break;
        case "<isMlgRush>":
          if (cachedMatch != null) {
            if (cachedMatch.getKit().isMlgRush()) {
              MatchTeam blueTeam = cachedMatch.getTeamById(1);
              MatchTeam redTeam = cachedMatch.getTeamById(0);
              scoreboard.getStringList("MLGRUSH-SPEC").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bRed>", PlayerUtil.getBridgesScore(blueTeam.getBridgesPoints(), true))
                      .replace("<rBed>", PlayerUtil.getBridgesScore(redTeam.getBridgesPoints(), false))

                  )
              );
            }
          }
          break;
        case "<isBedWars>":
          if (cachedMatch != null) {
            if (cachedMatch.getKit().isBedWars()) {
              MatchTeam blueTeam = cachedMatch.getTeamById(1);
              MatchTeam redTeam = cachedMatch.getTeamById(0);
              scoreboard.getStringList("BEDWARS-SPEC").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bBed>", PlayerUtil.getBedWarsScore(blueTeam.isHasBed(), true))
                      .replace("<rBed>", PlayerUtil.getBedWarsScore(redTeam.isHasBed(), false))
                  )
              );
            }
          }
          break;
        case "<isBattleRush>":
          if (cachedMatch != null) {
            if (cachedMatch.getKit().isBattleRush()) {
              MatchTeam blueTeam = cachedMatch.getTeamById(1);
              MatchTeam redTeam = cachedMatch.getTeamById(0);
              scoreboard.getStringList("BATTLERUSH-SPEC").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bGoal>",
                          PlayerUtil.getBattleRushScore(blueTeam.getBridgesPoints(), true))
                      .replace("<rGoal>",
                          PlayerUtil.getBattleRushScore(redTeam.getBridgesPoints(), false))
                  )
              );
            }
          }
          break;
        case "<isStickFight>":
          if (cachedMatch != null) {
            if (cachedMatch.getKit().isStickFight()) {
              MatchTeam blueTeam = cachedMatch.getTeamById(1);
              MatchTeam redTeam = cachedMatch.getTeamById(0);
              scoreboard.getStringList("STICKFIGHT-SPEC").forEach(a -> lines.add(CC.parse(player, a)
                      .replace("<bLives>", String.valueOf(blueTeam.getLives()))
                      .replace("<rLives>", String.valueOf(redTeam.getLives()))
                  )
              );
            }
          }
          break;
        default:
          switch (state) {
            case "SPECTATING":
              lines.add(replacementProvider.matchSpectatorReplace(text, cachedMatch));
              break;
            case "SPECTATING-PARTY-FFA":
            case "SPECTATING-PARTY":
              lines.add(replacementProvider.partyMatchSpectatorReplace(text, cachedMatch));
          }
      }
    });

    return lines;
  }

  private List<String> inFfaScoreboard(Player player) {
    List<String> strings = new LinkedList<>();

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() == PlayerState.FFA) {
      strings = scoreboard.getStringList("IN-SPAWN-FFA").stream()
          .map(string -> replacementProvider.spawnFfaReplace(string, player))
          .collect(Collectors.toCollection(LinkedList::new));
    }

    return strings;
  }

  private List<String> inLobbyScoreboard(Player player) {
    List<String> strings = new LinkedList<>();

    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getEventPlaying(player);
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.isInSpawn() && !practicePlayerData.isInEvent()) {
      if (party == null) {
        if (!practicePlayerData.isInEvent()
            && plugin.getManagerHandler().getTournamentManager().getTournaments().size() >= 1) {
          strings = placeholderLines("IN-TOURNAMENT", "TOURNAMENT", player);
        } else {
          strings = placeholderLines("IN-LOBBY", "LOBBY", player);
        }
      } else {
        if (!practicePlayerData.isInEvent()
            && plugin.getManagerHandler().getTournamentManager().getTournaments().size() >= 1) {
          strings = placeholderLines("IN-PARTY-TOURNAMENT", "PARTY-TOURNAMENT", player);
        } else {
          strings = placeholderLines("PARTY-IN-LOBBY", "PARTY", player);
        }
      }
    }

    try {
      PracticeEvent<?> specEvent = plugin.getManagerHandler().getEventManager().getOngoingEvent();
      if (practicePlayerData.isSpectating()) {
        if (plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
            .getCachedPlayer().get(player) != null) {
          PracticePlayerData cachedPlayerData = plugin.getManagerHandler().getPlayerManager()
              .getPlayerData(
                  plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
                      .getCachedPlayer().get(player).getUniqueId());
          Match cachedMatch = plugin.getManagerHandler().getMatchManager()
              .getMatch(cachedPlayerData.getUniqueId());
          Party cachedParty = plugin.getManagerHandler().getPartyManager()
              .getParty(cachedPlayerData.getUniqueId());
          if (cachedParty != null) {
            if (!cachedMatch.isFFA() && cachedMatch.isPartyMatch()) {
              strings = placeholderLinesSpectating("SPECTATING-MATCH-PARTY-SPLIT",
                  "SPECTATING-PARTY", player);
            } else if (!cachedMatch.isPartyMatch() && cachedMatch.isFFA()) {
              strings = placeholderLinesSpectating("SPECTATING-MATCH-PARTY-FFA",
                  "SPECTATING-PARTY-FFA", player);
            }
          } else {
            strings = placeholderLinesSpectating("SPECTATING-MATCH", "SPECTATING", player);
          }
        } else if (plugin.getManagerHandler().getEventManager().isSpectating(player)
            && specEvent != null) {
          if (specEvent instanceof LMSEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("LMS.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("LMS.PLAYING").stream()
                  .map(replacementProvider::lmsPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof KnockoutEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("KNOCKOUT.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("KNOCKOUT.PLAYING").stream()
                  .map(replacementProvider::knockoutPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof GulagEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("GULAG.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("GULAG.PLAYING").stream()
                  .map(replacementProvider::gulagPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof SumoEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("SUMO.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("SUMO.PLAYING").stream()
                  .map(replacementProvider::sumoPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof OITCEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("OITC.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("OITC.PLAYING").stream()
                  .map(string -> replacementProvider.oitcTopPlayingReplace(string, player))
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof TNTTagEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("TNT-TAG.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("TNT-TAG.PLAYING").stream()
                  .map(replacementProvider::tntTagPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof FourCornersEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("CORNERS.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("CORNERS.PLAYING").stream()
                  .map(replacementProvider::cornersPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof ThimbleEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("THIMBLE.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("THIMBLE.PLAYING").stream()
                  .map(replacementProvider::thimblePlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof DropperEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("DROPPER.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("DROPPER.PLAYING").stream()
                  .map(string -> replacementProvider.dropperPlayingReplace(string, player))
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof StopLightEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("STOPLIGHT.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("STOPLIGHT.PLAYING").stream()
                  .map(replacementProvider::stoplightPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof ParkourEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("PARKOUR.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("PARKOUR.PLAYING").stream()
                  .map(string -> replacementProvider.parkourPlayingReplace(string, player))
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof BracketsEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("BRACKETS.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("BRACKETS.PLAYING").stream()
                  .map(replacementProvider::bracketsPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          } else if (specEvent instanceof SpleefEvent) {
            if (specEvent.getState().equals(EventState.WAITING)) {
              strings = eventScoreboard.getStringList("SPLEEF.WAITING").stream()
                  .map(replacementProvider::eventWaitingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            } else if (specEvent.getState().equals(EventState.STARTED)) {
              strings = eventScoreboard.getStringList("SPLEEF.PLAYING").stream()
                  .map(replacementProvider::spleefPlayingReplace)
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          }
        } else {
          Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
          if (match != null) {
            Party playerParty = plugin.getManagerHandler().getPartyManager()
                .getParty(player.getUniqueId());
            if (playerParty != null) {
              if (match.isPartyMatch() && !match.isFFA()) {
                strings = scoreboard.getStringList("SPECTATING-MATCH-PARTY-SPLIT-AFTER-DEATH")
                    .stream()
                    .map(string -> replacementProvider.partyMatchSpectatorReplace(string, match))
                    .collect(Collectors.toCollection(LinkedList::new));
              } else if (match.isFFA() && !match.isPartyMatch()) {
                strings = scoreboard.getStringList("SPECTATING-MATCH-PARTY-FFA").stream()
                    .map(string -> replacementProvider.partyFfaMatchSpectatorReplace(string, match))
                    .collect(Collectors.toCollection(LinkedList::new));
              }
            } else {
              strings = scoreboard.getStringList("SPECTATING-MATCH-AFTER-DEATH").stream()
                  .map(string -> replacementProvider.matchSpectatorReplace(string, match))
                  .collect(Collectors.toCollection(LinkedList::new));
            }
          }
        }
      }

      if (practicePlayerData.isQueueing()) {
        QueueEntry queueEntry = party == null ? plugin.getManagerHandler().getQueueManager()
            .getQueueEntry(player.getUniqueId())
            : plugin.getManagerHandler().getQueueManager().getQueueEntry(party.getLeader());
        if (queueEntry != null) {
          if (queueEntry.getQueueType() == QueueType.UNRANKED) {
            strings = placeholderLines("IN-UNRANKED-QUEUE", "UNRANKED-QUEUE", player);
          } else if (queueEntry.getQueueType() == QueueType.RANKED) {
            strings = placeholderLines("IN-RANKED-QUEUE", "RANKED-QUEUE", player);
          } else if (queueEntry.getQueueType() == QueueType.PREMIUM) {
            strings = placeholderLines("IN-PREMIUM-QUEUE", "PREMIUM-QUEUE", player);
          }
        }
      }
    } catch (Exception ignored) {
    }

    if (event != null) {
      try {
        if (event instanceof LMSEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("LMS.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("LMS.PLAYING").stream()
                .map(replacementProvider::lmsPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof KnockoutEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("KNOCKOUT.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("KNOCKOUT.PLAYING").stream()
                .map(replacementProvider::knockoutPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof SkyWarsEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("SKYWARS.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("SKYWARS.PLAYING").stream()
                .map(replacementProvider::skyWarsPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof GulagEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("GULAG.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("GULAG.PLAYING").stream()
                .map(replacementProvider::gulagPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof SumoEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("SUMO.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("SUMO.PLAYING").stream()
                .map(replacementProvider::sumoPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof OITCEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("OITC.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("OITC.PLAYING").stream()
                .map(string -> replacementProvider.oitcTopPlayingReplace(string, player))
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof TNTTagEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("TNT-TAG.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("TNT-TAG.PLAYING").stream()
                .map(replacementProvider::tntTagPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof FourCornersEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("CORNERS.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("CORNERS.PLAYING").stream()
                .map(replacementProvider::cornersPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof ThimbleEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("THIMBLE.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("THIMBLE.PLAYING").stream()
                .map(replacementProvider::thimblePlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof DropperEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("DROPPER.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("DROPPER.PLAYING").stream()
                .map(string -> replacementProvider.dropperPlayingReplace(string, player))
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof StopLightEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("STOPLIGHT.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("STOPLIGHT.PLAYING").stream()
                .map(replacementProvider::stoplightPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof ParkourEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("PARKOUR.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("PARKOUR.PLAYING").stream()
                .map(string -> replacementProvider.parkourPlayingReplace(string, player))
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof BracketsEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("BRACKETS.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("BRACKETS.PLAYING").stream()
                .map(replacementProvider::bracketsPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        } else if (event instanceof SpleefEvent) {
          if (event.getState().equals(EventState.WAITING)) {
            strings = eventScoreboard.getStringList("SPLEEF.WAITING").stream()
                .map(replacementProvider::eventWaitingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          } else if (event.getState().equals(EventState.STARTED)) {
            strings = eventScoreboard.getStringList("SPLEEF.PLAYING").stream()
                .map(replacementProvider::spleefPlayingReplace)
                .collect(Collectors.toCollection(LinkedList::new));
          }
        }
      } catch (Exception ignored) {
      }
    }

    return strings;
  }

  private List<String> inMatchScoreboard(Player player) {
    List<String> strings = new LinkedList<>();
    Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    if (!match.isPartyMatch() && !match.isFFA()) {
      if (match.getMatchState() == MatchState.STARTING
          || match.getMatchState() == MatchState.FIGHTING) {
        if (practicePlayerData.getPlayerSettings().isPingScoreboardToggled()) {
          strings = placeholderLines("IN-MATCH-PING", "MATCH-PING", player);
        } else {
          strings = placeholderLines("IN-MATCH", "MATCH", player);
        }
      } else if (match.getMatchState() == MatchState.ENDING) {
        strings = scoreboard.getStringList("IN-MATCH-END").stream()
            .map(string -> replacementProvider.matchReplace(string, player))
            .collect(Collectors.toCollection(LinkedList::new));
      }
    } else if (match.isPartyMatch() && !match.isFFA()) {
      strings = placeholderLines("IN-MATCH-PARTY-SPLIT", "PARTY-SPLIT", player);
    } else if (match.isFFA() && !match.isPartyMatch()) {
      strings = placeholderLines("IN-MATCH-PARTY-FFA", "PARTY-FFA", player);
    }

    return strings;
  }

  private boolean hasExpiredEnderCooldown(Player player) {
    long cooldown = Frost.getInstance().getManagerHandler().getTimerManager()
        .getTimer(EnderpearlTimer.class).getRemaining(player);
    return !(cooldown > 0);
  }

  @Override
  public void onScoreboardCreate(Player player, Scoreboard scoreboard) {
    if (scoreboard != null) {
      Team red = scoreboard.getTeam("red");
      if (red == null) {
        red = scoreboard.registerNewTeam("red");
      }

      Team green = scoreboard.getTeam("green");
      if (green == null) {
        green = scoreboard.registerNewTeam("green");
      }

      Team blue = scoreboard.getTeam("blue");
      if (blue == null) {
        blue = scoreboard.registerNewTeam("blue");
      }

      red.setPrefix(String.valueOf(ChatColor.RED));
      green.setPrefix(String.valueOf(ChatColor.GREEN));
      blue.setPrefix(String.valueOf(ChatColor.BLUE));

      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (practicePlayerData.getPlayerState() != PlayerState.FIGHTING) {
        Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);
        if (objective != null) {
          objective.unregister();
        }

        for (String entry : red.getEntries()) {
          red.removeEntry(entry);
        }
        for (String entry : green.getEntries()) {
          green.removeEntry(entry);
        }
        for (String entry : blue.getEntries()) {
          blue.removeEntry(entry);
        }

        if (plugin.getSettingsConfig().getConfig().getBoolean("SETTINGS.GENERAL.NAMETAGS-ENABLED")
            && Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
          for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == null) {
              return;
            }

            Team spawn = scoreboard.getTeam(online.getName());
            if (spawn == null) {
              spawn = scoreboard.registerNewTeam(online.getName());
            }

            if (online == player) {
              spawn.setPrefix(CC.parse(player, plugin.getSettingsConfig().getConfig()
                  .getString("SETTINGS.GENERAL.NAMETAGS-PREFIX")));
            } else {
              spawn.setPrefix(CC.parse(online, plugin.getSettingsConfig().getConfig()
                  .getString("SETTINGS.GENERAL.NAMETAGS-PREFIX")));
            }

            String onlinePlayer = online.getName();
            if (spawn.hasEntry(onlinePlayer)) {
              continue;
            }
            spawn.addEntry(onlinePlayer);

            return;
          }
        }

        return;
      }

      Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
      if (match.getKit().isShowHealth()) {
        Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);
        if (objective == null) {
          objective = player.getScoreboard().registerNewObjective("showhealth", "health");
        }

        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName(ChatColor.RED + StringEscapeUtils.unescapeJava("\u2764"));
        objective.getScore(player.getName()).setScore((int) Math.floor(player.getHealth()));
      }

      for (MatchTeam team : match.getTeams()) {
        for (UUID teamUUID : team.getAlivePlayers()) {
          Player teamPlayer = plugin.getServer().getPlayer(teamUUID);
          if (teamPlayer != null) {
            String teamPlayerName = teamPlayer.getName();
            if (match.getKit().isBridges() || match.getKit().isBattleRush() || match.getKit()
                .isMlgRush() || match.getKit().isBedWars() || match.getKit().isStickFight()) {
              if (team.getTeamID() == 1) {
                if (blue.hasEntry(teamPlayerName)) {
                  continue;
                }
                blue.addEntry(teamPlayerName);
              } else {
                if (red.hasEntry(teamPlayerName)) {
                  continue;
                }
                red.addEntry(teamPlayerName);
              }
            } else {
              if (team.getTeamID() == practicePlayerData.getTeamId() && !match.isFFA()) {
                if (green.hasEntry(teamPlayerName)) {
                  continue;
                }
                green.addEntry(teamPlayerName);
              } else {
                if (red.hasEntry(teamPlayerName)) {
                  continue;
                }
                red.addEntry(teamPlayerName);
              }
            }
          }
        }
      }
    }
  }
}
