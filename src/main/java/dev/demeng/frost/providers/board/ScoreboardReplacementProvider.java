package dev.demeng.frost.providers.board;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.brackets.BracketsEvent;
import dev.demeng.frost.events.games.corners.FourCornersEvent;
import dev.demeng.frost.events.games.dropper.DropperEvent;
import dev.demeng.frost.events.games.gulag.GulagEvent;
import dev.demeng.frost.events.games.knockout.KnockoutEvent;
import dev.demeng.frost.events.games.lms.LMSEvent;
import dev.demeng.frost.events.games.oitc.OITCEvent;
import dev.demeng.frost.events.games.oitc.OITCPlayer;
import dev.demeng.frost.events.games.parkour.ParkourEvent;
import dev.demeng.frost.events.games.parkour.ParkourPlayer;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.events.games.spleef.SpleefEvent;
import dev.demeng.frost.events.games.stoplight.StopLightEvent;
import dev.demeng.frost.events.games.sumo.SumoEvent;
import dev.demeng.frost.events.games.thimble.ThimbleEvent;
import dev.demeng.frost.events.games.tnttag.TNTTagEvent;
import dev.demeng.frost.game.ffa.FfaInstance;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.queue.QueueEntry;
import dev.demeng.frost.game.tournament.Tournament;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.RatingUtil;
import dev.demeng.frost.util.TimeUtils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreboardReplacementProvider {

  public final Frost plugin = Frost.getInstance();

  public String spawnReplace(String string, Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    return string
        .replace("<online_players>", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
        .replace("<queueing>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))
        .replace("<premium_matches>", String.valueOf(practicePlayerData.getPremiumMatches()))
        .replace("<global_elo>", String.valueOf(practicePlayerData.getGlobalElo()))
        .replace("<global_elo_rating>",
            String.valueOf(RatingUtil.getRankByElo(practicePlayerData.getGlobalElo()).getName()))
        ;
  }

  public String spawnFfaReplace(String string, Player player) {
    FfaInstance ffa = plugin.getManagerHandler().getFfaManager().getByPlayer(player);

    return string
        .replace("<kit>", ffa.getKit().getName())
        .replace("<ks>", String.valueOf(ffa.getKillStreakTracker().get(player.getUniqueId())))
        .replace("<kit_ffa_players>", String.valueOf(ffa.getFfaPlayers().size()))
        .replace("<ping>", String.valueOf(PlayerUtil.getPing(player)))
        ;
  }

  public String partySpawnReplace(String string, Player player) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    return string
        .replace("<online_players>", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
        .replace("<queueing>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))
        .replace("<premium_matches>", String.valueOf(practicePlayerData.getPremiumMatches()))
        .replace("<global_elo>", String.valueOf(practicePlayerData.getGlobalElo()))
        .replace("<party_members>", String.valueOf(party.getMembers().size()))
        .replace("<party_max>", String.valueOf(party.getLimit()))
        .replace("<party_leader>", plugin.getServer().getPlayer(party.getLeader()).getName())
        ;
  }

  public String tournamentSpawnReplace(String string, Player player) {
    Tournament tournament = plugin.getManagerHandler().getTournamentManager().getTournaments()
        .get(0);
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    return string
        .replace("<online_players>", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
        .replace("<queueing>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))
        .replace("<global_elo>", String.valueOf(practicePlayerData.getGlobalElo()))
        .replace("<tournament_kit>", String.valueOf(tournament.getKitName()))
        .replace("<tournament_round>", String.valueOf(tournament.getCurrentRound()))
        .replace("<tournament_players>", String.valueOf(tournament.getPlayers().size()))
        .replace("<tournament_max_players>", String.valueOf(tournament.getSize()))
        .replace("<tournament_team_size>", String.valueOf(tournament.getTeamSize()))
        ;
  }

  public String partyTournamentSpawnReplace(String string, Player player) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    Tournament tournament = plugin.getManagerHandler().getTournamentManager().getTournaments()
        .get(0);
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    return string
        .replace("<online_players>", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
        .replace("<queueing>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))
        .replace("<global_elo>", String.valueOf(practicePlayerData.getGlobalElo()))
        .replace("<party_members>", String.valueOf(party.getMembers().size()))
        .replace("<party_max>", String.valueOf(party.getLimit()))
        .replace("<party_leader>", plugin.getServer().getPlayer(party.getLeader()).getName())
        .replace("<tournament_kit>", String.valueOf(tournament.getKitName()))
        .replace("<tournament_round>", String.valueOf(tournament.getCurrentRound()))
        .replace("<tournament_players>", String.valueOf(tournament.getPlayers().size()))
        .replace("<tournament_max_players>", String.valueOf(tournament.getSize()))
        .replace("<tournament_team_size>", String.valueOf(tournament.getTeamSize()))
        ;
  }

  public String unrankedQueueReplace(String string, Player player) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    QueueEntry queueEntry = party == null ? plugin.getManagerHandler().getQueueManager()
        .getQueueEntry(player.getUniqueId())
        : plugin.getManagerHandler().getQueueManager().getQueueEntry(party.getLeader());
    if (queueEntry == null) {
      return "QUEUE_ERROR";
    }

    long queueTime =
        System.currentTimeMillis() - (party == null ? plugin.getManagerHandler().getQueueManager()
            .getPlayerQueueTime(player.getUniqueId())
            : plugin.getManagerHandler().getQueueManager().getPlayerQueueTime(party.getLeader()));
    String formattedQueueTime = TimeUtils.formatIntoMMSS(Math.round(queueTime / 1000L));

    return string
        .replace("<online_players>", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
        .replace("<queueing>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))
        .replace("<global_elo>", String.valueOf(practicePlayerData.getGlobalElo()))
        .replace("<queued_time>", formattedQueueTime)
        .replace("<queued_type>", queueEntry.getQueueType().getName())
        .replace("<queued_kit>", queueEntry.getKitName())
        .replace("<queued_kit_queueing>", String.valueOf(
            plugin.getManagerHandler().getQueueManager()
                .getQueueSize(queueEntry.getKitName(), queueEntry.getQueueType())))
        .replace("<queued_kit_fighting>", String.valueOf(
            plugin.getManagerHandler().getMatchManager()
                .getFighters(queueEntry.getKitName(), queueEntry.getQueueType())))
        ;
  }

  public String rankedQueueReplace(String string, Player player) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    QueueEntry queueEntry = party == null ? plugin.getManagerHandler().getQueueManager()
        .getQueueEntry(player.getUniqueId())
        : plugin.getManagerHandler().getQueueManager().getQueueEntry(party.getLeader());
    if (queueEntry == null) {
      return "QUEUE_ERROR";
    }

    long queueTime =
        System.currentTimeMillis() - (party == null ? plugin.getManagerHandler().getQueueManager()
            .getPlayerQueueTime(player.getUniqueId())
            : plugin.getManagerHandler().getQueueManager().getPlayerQueueTime(party.getLeader()));
    String formattedQueueTime = TimeUtils.formatIntoMMSS(Math.round(queueTime / 1000L));

    return string
        .replace("<online_players>", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
        .replace("<queueing>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))
        .replace("<global_elo>", String.valueOf(practicePlayerData.getGlobalElo()))
        .replace("<kit_elo>", String.valueOf(practicePlayerData.getElo(queueEntry.getKitName())))

        .replace("<queued_time>", formattedQueueTime)
        .replace("<queued_type>",
            (queueEntry.getQueueType().getName() != null ? queueEntry.getQueueType().getName()
                : "QUEUE_ERROR"))
        .replace("<queued_kit>", queueEntry.getKitName())

        .replace("<premium_matches>", String.valueOf(practicePlayerData.getPremiumMatches()))

        .replace("<queued_kit_queueing>", String.valueOf(
            plugin.getManagerHandler().getQueueManager()
                .getQueueSize(queueEntry.getKitName(), queueEntry.getQueueType())))
        .replace("<queued_kit_fighting>", String.valueOf(
            plugin.getManagerHandler().getMatchManager()
                .getFighters(queueEntry.getKitName(), queueEntry.getQueueType())))
        ;
  }

  public String matchSpectatorReplace(String string, Match match) {
    List<MatchTeam> teams = match.getTeams();
    MatchTeam firstTeam = teams.get(0);
    MatchTeam secondTeam = teams.get(1);

    return string
        .replace("<playerA>", firstTeam.getLeaderName())
        .replace("<playerB>", secondTeam.getLeaderName())
        .replace("<playerA_ping>", String.valueOf(PlayerUtil.getPing(firstTeam.getLeaderName())))
        .replace("<playerB_ping>", secondTeam.getLeaderName() == null ? "0"
            : String.valueOf(PlayerUtil.getPing(secondTeam.getLeaderName())))
        .replace("<isRanked>", (match.getType().isRanked() ? "&aTrue" : "&cFalse"))
        .replace("<arenaName>", match.getArena().getName())
        .replace("<kitName>", match.getKit().getName())
        .replace("<match_duration>", String.valueOf(match.getDuration()))
        ;
  }

  public String partyMatchSpectatorReplace(String string, Match match) {
    List<MatchTeam> teams = match.getTeams();
    MatchTeam firstTeam = teams.get(0);
    MatchTeam secondTeam = teams.get(1);

    return string
        .replace("<playerA>", firstTeam.getLeaderName())
        .replace("<playerB>", secondTeam.getLeaderName())
        .replace("<arenaName>", match.getArena().getName())
        .replace("<kitName>", match.getKit().getName())
        .replace("<match_duration>", String.valueOf(match.getDuration()))
        ;
  }

  public String partyFfaMatchSpectatorReplace(String string, Match match) {
    List<MatchTeam> teams = match.getTeams();
    MatchTeam firstTeam = teams.get(0);

    return string
        .replace("<party_leader>", firstTeam.getLeaderName())
        .replace("<players>", String.valueOf(firstTeam.getPlayers().size()))
        .replace("<alive>", String.valueOf(firstTeam.getAlivePlayers().size()))
        .replace("<arenaName>", match.getArena().getName())
        .replace("<kitName>", match.getKit().getName())
        .replace("<match_duration>", String.valueOf(match.getDuration()))
        ;
  }

  public String matchReplace(String string, Player player) {
    Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
    Player opponentPlayer =
        match.getTeams().get(0).getPlayers().get(0) == player.getUniqueId() ? plugin.getServer()
            .getPlayer(match.getTeams().get(1).getPlayers().get(0))
            : plugin.getServer().getPlayer(match.getTeams().get(0).getPlayers().get(0));

    return string
        .replace("<opponent_name>",
            opponentPlayer == null ? "Rival Left..." : opponentPlayer.getName())
        .replace("<match_duration>", String.valueOf(match.getDuration()))
        .replace("<match_kit>", match.getKit().getName())
        .replace("<match_arena>", match.getArena().getName())
        .replace("<your_hits>", String.valueOf(
            plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
                .getHits()))
        .replace("<your_ping>", String.valueOf(PlayerUtil.getPing(player)))
        .replace("<opponent_ping>",
            opponentPlayer == null ? "0" : String.valueOf(PlayerUtil.getPing(opponentPlayer)))
        ;
  }

  public String partyMatchReplace(String string, Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
    MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
        : (practicePlayerData.getTeamId() == 0 ? match.getTeams().get(1) : match.getTeams().get(0));
    MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());

    return string
        .replace("<match_duration>", String.valueOf(match.getDuration()))
        .replace("<match_kit>", match.getKit().getName())
        .replace("<match_arena>", match.getArena().getName())
        .replace("<your_ping>", String.valueOf(PlayerUtil.getPing(player)))
        .replace("<your_team>", String.valueOf(playerTeam.getPlayers().size()))
        .replace("<your_team_alive>", String.valueOf(playerTeam.getAlivePlayers().size()))
        .replace("<opponent_team>", String.valueOf(opposingTeam.getPlayers().size()))
        .replace("<opponent_team_alive>", String.valueOf(opposingTeam.getAlivePlayers().size()))
        .replace("<ffa_alive>",
            String.valueOf(match.getTeams().get(0).getAlivePlayers().size() - 1))
        ;
  }

  public String eventWaitingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();

    return string
        .replace("<event_host>", event.getHost().getName())
        .replace("<event_max>", String.valueOf(event.getLimit()))
        .replace("<event_kit>",
            event.getKitOptional().isPresent() ? event.getKitOptional().get().getName() : "None")
        .replace("<event_joined>", String.valueOf(event.getPlayers().size()))
        .replace("<event_countdown>", String.valueOf(event.getCountdownTask().getTimeUntilStart()));
  }

  public String lmsPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    LMSEvent lmsEvent = (LMSEvent) event;

    return string
        .replace("<alive_players>", String.valueOf(lmsEvent.getPlayers().size()))
        .replace("<event_max>", String.valueOf(lmsEvent.getLimit()));
  }

  public String knockoutPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    KnockoutEvent knockoutEvent = (KnockoutEvent) event;

    return string
        .replace("<alive_players>", String.valueOf(knockoutEvent.getPlayers().size()))
        .replace("<event_max>", String.valueOf(knockoutEvent.getLimit()));
  }

  public String skyWarsPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    SkyWarsEvent skyWarsEvent = (SkyWarsEvent) event;

    return string
        .replace("<alive_players>", String.valueOf(skyWarsEvent.getPlayers().size()))
        .replace("<event_kit>",
            skyWarsEvent.getKitOptional().isPresent() ? skyWarsEvent.getKitOptional().get()
                .getName() : "None")
        .replace("<event_host>", skyWarsEvent.getHost().getName())
        .replace("<event_max>", String.valueOf(skyWarsEvent.getLimit())
        );
  }

  public String sumoPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    SumoEvent sumoEvent = (SumoEvent) event;

    return string
        .replace("<playerA>",
            (sumoEvent.getFighting().get(0).getName() != null ? sumoEvent.getFighting().get(0)
                .getName() : "None"))
        .replace("<playerB>",
            (sumoEvent.getFighting().get(1).getName() != null ? sumoEvent.getFighting().get(1)
                .getName() : "None"))
        .replace("<playerA_ping>",
            String.valueOf(PlayerUtil.getPing(sumoEvent.getFighting().get(0))))
        .replace("<playerB_ping>",
            String.valueOf(PlayerUtil.getPing(sumoEvent.getFighting().get(1))))
        .replace("<current_round>", String.valueOf(sumoEvent.getRound()))
        .replace("<alive_players>", String.valueOf(sumoEvent.getPlayers().size()))
        .replace("<event_max>", String.valueOf(sumoEvent.getLimit()));
  }

  public String oitcTopPlayingReplace(String string, Player player) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getEventPlaying(player);
    OITCEvent oitcEvent = (OITCEvent) event;
    OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);

    List<OITCPlayer> sortedList = oitcEvent.sortedScores();
    Player first = Bukkit.getPlayer(sortedList.get(0).getUuid());
    Player second = Bukkit.getPlayer(sortedList.get(1).getUuid());
    Player third = Bukkit.getPlayer(sortedList.get(2).getUuid());

    return string
        .replace("<event_max>", String.valueOf(oitcEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(oitcEvent.getPlayers().size()))

        .replace("<first_place>", first.getName())
        .replace("<second_place>", second.getName())
        .replace("<third_place>", third.getName() == null ? "None" : third.getName())

        .replace("<first_place_score>", String.valueOf(sortedList.get(0).getScore()))
        .replace("<second_place_score>", String.valueOf(sortedList.get(1).getScore()))
        .replace("<third_place_score>",
            (sortedList.get(2) != null ? String.valueOf(sortedList.get(2).getScore()) : "0"))

        .replace("<player_score>",
            String.valueOf(oitcPlayer != null ? oitcPlayer.getScore() : "Not Playing!"))
        ;
  }

  public String cornersPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    FourCornersEvent fourCornersEvent = (FourCornersEvent) event;

    return string
        .replace("<current_round>", String.valueOf(fourCornersEvent.getRound()))
        .replace("<next_round>", String.valueOf(fourCornersEvent.getSeconds()))
        .replace("<event_max>", String.valueOf(fourCornersEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(fourCornersEvent.getPlayers().size()))
        ;
  }

  public String thimblePlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    ThimbleEvent thimbleEvent = (ThimbleEvent) event;

    return string
        .replace("<current_round>", String.valueOf(thimbleEvent.getRound()))
        .replace("<event_max>", String.valueOf(thimbleEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(thimbleEvent.getPlayers().size()))
        ;
  }

  public String dropperPlayingReplace(String string, Player player) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    DropperEvent dropperEvent = (DropperEvent) event;

    return string
        .replace("<current_map>", String.valueOf(dropperEvent.getPlayer(player).getPhase()))
        .replace("<maps_total>", String.valueOf(dropperEvent.getSpawnLocations().size()))
        .replace("<alive_players>", String.valueOf(dropperEvent.getPlayers().size()))
        .replace("<event_max>", String.valueOf(dropperEvent.getLimit()))
        ;
  }

  public String stoplightPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    StopLightEvent stopLightEvent = (StopLightEvent) event;

    return string
        .replace("<status>",
            stopLightEvent.getCurrent().toString().equalsIgnoreCase("STOP") ? "&cSTOP" : "&aGO")
        .replace("<event_max>", String.valueOf(stopLightEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(stopLightEvent.getPlayers().size()))
        ;
  }

  public String spleefPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    SpleefEvent spleefEvent = (SpleefEvent) event;

    return string
        .replace("<event_max>", String.valueOf(spleefEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(spleefEvent.getPlayers().size()))
        ;
  }

  public String bracketsPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    BracketsEvent bracketsEvent = (BracketsEvent) event;

    return string
        .replace("<playerA>", bracketsEvent.getFighting().get(0).getName())
        .replace("<playerB>", bracketsEvent.getFighting().get(1).getName())
        .replace("<playerA_ping>",
            String.valueOf(PlayerUtil.getPing(bracketsEvent.getFighting().get(0))))
        .replace("<playerB_ping>",
            String.valueOf(PlayerUtil.getPing(bracketsEvent.getFighting().get(1))))

        .replace("<current_round>", String.valueOf(bracketsEvent.getRound()))
        .replace("<event_max>", String.valueOf(bracketsEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(bracketsEvent.getPlayers().size()))
        ;
  }

  public String gulagPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    GulagEvent gulagEvent = (GulagEvent) event;

    return string
        .replace("<playerA>", gulagEvent.getFighting().get(0).getName())
        .replace("<playerB>", gulagEvent.getFighting().get(1).getName())
        .replace("<playerA_ping>",
            String.valueOf(PlayerUtil.getPing(gulagEvent.getFighting().get(0))))
        .replace("<playerB_ping>",
            String.valueOf(PlayerUtil.getPing(gulagEvent.getFighting().get(1))))

        .replace("<current_round>", String.valueOf(gulagEvent.getRound()))
        .replace("<event_max>", String.valueOf(gulagEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(gulagEvent.getPlayers().size()))
        ;
  }

  public String tntTagPlayingReplace(String string) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    TNTTagEvent tntTagEvent = (TNTTagEvent) event;

    return string
        .replace("<tnt_time>", String.valueOf(tntTagEvent.getTntTagTask().getTime()))
        .replace("<event_max>", String.valueOf(tntTagEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(tntTagEvent.getPlayers().size()))
        ;
  }

  public String parkourPlayingReplace(String string, Player player) {
    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    ParkourEvent parkourEvent = (ParkourEvent) event;
    ParkourPlayer parkourPlayer = parkourEvent.getPlayer(player);

    return string
        .replace("<checkpoint_id>", String.valueOf(parkourPlayer.getCheckpointId()))
        .replace("<event_max>", String.valueOf(parkourEvent.getLimit()))
        .replace("<alive_players>", String.valueOf(parkourEvent.getPlayers().size()))
        ;
  }
}
