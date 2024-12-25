package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchRequest;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.handler.ManagerHandler;
import dev.demeng.frost.managers.PartyManager;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Usage;


public class MatchCommands {

  @Dependency private final Frost plugin;
  private final ConfigCursor errorMessage;
  private final ManagerHandler handler;

  public MatchCommands(Frost plugin) {
    this.plugin = plugin;

    errorMessage = new ConfigCursor(plugin.getMessagesConfig(), "ERROR-MESSAGES.PLAYER");
    handler = plugin.getManagerHandler();
  }

  @Command("duel")
  @Usage("Usage: /duel <player>")
  public void sendDuelRequest(Player player, Player target) {
    if (handler.getTournamentManager().getTournament(player.getUniqueId()) != null) {
      CC.sendMessage(player, errorMessage.getString("IN-TOURNAMENT"));
      return;
    }

    PracticePlayerData practicePlayerData = handler.getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return;
    }
    if (handler.getTournamentManager().getTournament(target.getUniqueId()) != null) {
      CC.sendMessage(player, errorMessage.getString("TARGET-IN-TOURNAMENT"));
      return;
    }
    if (handler.getTournamentManager().isInTournament(target.getUniqueId())) {
      CC.sendMessage(player, errorMessage.getString("TARGET-IN-TOURNAMENT"));
      return;
    }

    Party party = handler.getPartyManager().getParty(player.getUniqueId());
    if ((party != null && handler.getPartyManager().isInParty(target.getUniqueId(), party))
        || player.getName().equals(target.getName())) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-DUEL-YOURSELF"));
      return;
    }
    if (party != null && !handler.getPartyManager().isLeader(player.getUniqueId())) {
      CC.sendMessage(player, errorMessage.getString("NOT-PARTY-LEADER"));
      return;
    }

    PracticePlayerData targetData = handler.getPlayerManager().getPlayerData(target.getUniqueId());
    if (targetData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("CURRENTLY-BUSY"));
      return;
    }
    if (!targetData.getPlayerSettings().isDuelRequests()) {
      CC.sendMessage(player, errorMessage.getString("CURRENTLY-IGNORING-DUELS"));
      return;
    }

    Party targetParty = handler.getPartyManager().getParty(target.getUniqueId());
    if (party == null && targetParty != null) {
      CC.sendMessage(player, errorMessage.getString("TARGET-ALREADY-IN-PARTY"));
      return;
    }
    if (party != null && targetParty == null) {
      CC.sendMessage(player, errorMessage.getString("ALREADY-IN-PARTY"));
      return;
    }

    practicePlayerData.setDuelSelecting(target.getUniqueId());
    player.openInventory(handler.getInventoryManager().getDuelInventory().getCurrentPage());
  }

  @Command("accept")
  @Usage("Usage: /accept <player>")
  public void acceptDuelRequest(Player player, Player target, String kitName) {
    PracticePlayerData practicePlayerData = handler.getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("UNABLE-TO-ACCEPT-DUEL"));
      return;
    }
    if (player.getName().equals(target.getName())) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-DUEL-YOURSELF"));
      return;
    }

    PracticePlayerData targetData = handler.getPlayerManager().getPlayerData(target.getUniqueId());
    if (targetData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("CURRENTLY-BUSY"));
      return;
    }
    if (handler.getTournamentManager().isInTournament(target.getUniqueId())) {
      CC.sendMessage(player, errorMessage.getString("TARGET-IN-TOURNAMENT"));
      return;
    }
    if (handler.getTournamentManager().isInTournament(player.getUniqueId())) {
      CC.sendMessage(player, errorMessage.getString("IN-TOURNAMENT"));
      return;
    }

    MatchRequest request = handler.getMatchManager()
        .getMatchRequest(target.getUniqueId(), player.getUniqueId());
    Kit kit = handler.getKitManager().getKit(kitName);
    if (kit != null) {
      request = handler.getMatchManager()
          .getMatchRequest(target.getUniqueId(), player.getUniqueId(), kit.getName());
    }
    if (request == null) {
      CC.sendMessage(player, errorMessage.getString("NO-PENDING-REQUESTS"));
      return;
    }
    if (request.getRequester().equals(target.getUniqueId())) {
      List<UUID> playersA = new ArrayList<>();
      List<UUID> playersB = new ArrayList<>();
      PartyManager partyManager = handler.getPartyManager();
      Party party = partyManager.getParty(player.getUniqueId());
      Party targetParty = partyManager.getParty(target.getUniqueId());
      if (request.isParty()) {
        if (party == null || targetParty == null || !partyManager.isLeader(target.getUniqueId())
            || !partyManager.isLeader(target.getUniqueId())) {
          CC.sendMessage(player, errorMessage.getString("TARGET-NOT-PARTY-LEADER"));
          return;
        }
        playersA.addAll(party.getMembers());
        playersB.addAll(targetParty.getMembers());
      } else {
        if (party != null || targetParty != null) {
          CC.sendMessage(player, errorMessage.getString("TARGET-ALREADY-IN-PARTY"));
          return;
        }
        playersA.add(player.getUniqueId());
        playersB.add(target.getUniqueId());
      }

      Kit kit2 = handler.getKitManager().getKit(request.getKitName());
      MatchTeam teamA = new MatchTeam(target.getUniqueId(), playersB, 0);
      MatchTeam teamB = new MatchTeam(player.getUniqueId(), playersA, 1);
      Player leaderA = plugin.getServer().getPlayer(teamA.getLeader());
      Player leaderB = plugin.getServer().getPlayer(teamB.getLeader());
      Match match = new Match(request.getArena(), kit2, QueueType.UNRANKED, teamA, teamB);

      match.broadcast(
          plugin.getMessagesConfig().getConfig().getString("MESSAGES.DUEL.ACCEPTED-STARTING")
              .replace("<kit>", kit2.getName())
              .replace("<playerA>",
                  (match.isPartyMatch() ? leaderA.getName() + "'s Party" : leaderA.getName()))
              .replace("<playerB>",
                  (match.isPartyMatch() ? leaderB.getName() + "'s Party" : leaderB.getName()))
      );

      handler.getMatchManager().createMatch(match);
    }
  }

  @Command({"spectate", "spec"})
  @Usage("Usage: /spectate <player>")
  public void spectatePlayer(Player player, Player target) {
    PracticePlayerData practicePlayerData = handler.getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = handler.getPartyManager().getParty(practicePlayerData.getUniqueId());
    if (party != null || (practicePlayerData.getPlayerState() != PlayerState.SPAWN
        && practicePlayerData.getPlayerState() != PlayerState.SPECTATING)) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return;
    }

    PracticePlayerData targetData = handler.getPlayerManager().getPlayerData(target.getUniqueId());
    if (targetData.getPlayerState() != PlayerState.FIGHTING) {
      CC.sendMessage(player, errorMessage.getString("TARGET-NOT-IN-FIGHT"));
      return;
    }

    Match targetMatch = handler.getMatchManager().getMatch(targetData);
    if (!targetMatch.isParty()) {
      if (!targetData.getPlayerSettings().isSpectatorsAllowed() && !player.hasPermission(
          "frost.staff")) {
        CC.sendMessage(player, errorMessage.getString("DOES-NOT-ALLOW-SPECTATORS"));
        return;
      }

      MatchTeam team = targetMatch.getTeams().get(0);
      MatchTeam team2 = targetMatch.getTeams().get(1);
      PracticePlayerData otherPracticePlayerData = handler.getPlayerManager()
          .getPlayerData(team.getPlayers().get(0) == target.getUniqueId()
              ? team2.getPlayers().get(0)
              : team.getPlayers().get(0)
          );

      if (otherPracticePlayerData != null && !otherPracticePlayerData.getPlayerSettings()
          .isSpectatorsAllowed() && !player.hasPermission("frost.staff")) {
        CC.sendMessage(player, errorMessage.getString("DOES-NOT-ALLOW-SPECTATORS"));
        return;
      }
    }

    if (practicePlayerData.getPlayerState() == PlayerState.SPECTATING) {
      Match match = handler.getMatchManager().getSpectatingMatch(player.getUniqueId());
      if (match.equals(targetMatch)) {
        CC.sendMessage(player, errorMessage.getString("ALREADY-SPECTATING"));
        return;
      }

      match.removeSpectator(player.getUniqueId());
      practicePlayerData.getCachedPlayer().clear();
    }

    CC.sendMessage(player,
        plugin.getMessagesConfig().getConfig().getString("MESSAGES.MATCH.SPECTATING")
            .replace("<player>", target.getName()));
    practicePlayerData.getCachedPlayer().put(player, target);
    handler.getMatchManager().addSpectator(player, practicePlayerData, target, targetMatch);
  }

  @Command({"requeue", "playagain"})
  public void requeue(Player player) {
    PracticePlayerData playerData = handler.getPlayerManager().getPlayerData(player.getUniqueId());
    if (!handler.getMatchManager().hasPlayAgainRequest(player.getUniqueId())) {
      player.sendMessage(CC.color("&cThe time for requeuing has expired."));
      return;
    }

    Kit kit = handler.getMatchManager().getPlayAgainRequestKit(playerData.getUniqueId());
    if (kit == null) {
      player.sendMessage(CC.color("&cThat kit doesn't exist!"));
      return;
    }

    if (playerData.isInSpawn()) {
      handler.getQueueManager()
          .addPlayerToQueue(player, playerData, kit.getName(), QueueType.UNRANKED);
    } else if (playerData.isInMatch() || playerData.isSpectating()) {
      Match match = handler.getMatchManager().getMatch(playerData);
      if (match != null) {
        match.getTeams().forEach(team -> team.killPlayer(playerData.getUniqueId()));
        match.getSpectators().remove(playerData.getUniqueId());
      }

      handler.getPlayerManager().resetPlayerOrSpawn(player, true);
      handler.getQueueManager()
          .addPlayerToQueue(player, playerData, kit.getName(), QueueType.UNRANKED);
    }

    if (player.getItemInHand()
        .isSimilar(handler.getItemManager().getPlayAgainItem().getItemStack())) {
      player.setItemInHand(new ItemStack(Material.AIR));
    }

    handler.getMatchManager().removePlayAgainRequest(playerData.getUniqueId());
  }
}
