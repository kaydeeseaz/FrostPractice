package dev.demeng.frost.game.match.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.event.match.MatchEndEvent;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.managers.PlayerManager;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.player.match.MatchLocatedData;
import dev.demeng.frost.user.ui.postmatch.InventorySnapshot;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.EloUtil;
import dev.demeng.frost.util.MathUtil;
import dev.demeng.frost.util.RatingUtil;
import dev.demeng.frost.util.TimeUtils;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.github.paperspigot.Title;

public class MatchEndListener implements Listener {

  private final Frost plugin = Frost.getInstance();
  private final ConfigCursor messages = new ConfigCursor(Frost.getInstance().getMessagesConfig(),
      "MESSAGES.MATCH");

  private double health;

  @EventHandler
  public void onMatchEnd(MatchEndEvent event) {
    Match match = event.getMatch();
    match.setMatchState(MatchState.ENDING);
    match.setWinningTeamId(event.getWinningTeam().getTeamID());
    match.setCountdown(4);

    List<UUID> spectators = new ArrayList<>(match.getSpectators());
    for (UUID spec : spectators) {
      if (!Frost.getInstance().getManagerHandler().getPlayerManager().getPlayerData(spec)
          .getCachedPlayer().isEmpty()) {
        Frost.getInstance().getManagerHandler().getPlayerManager().getPlayerData(spec)
            .getCachedPlayer().clear();
      }
    }

    match.getTeams().forEach(team -> team.players().forEach(player -> {
      if (!match.hasSnapshot(player.getUniqueId())) {
        match.addSnapshot(player);
        health = Bukkit.getPlayer(match.getTeamById(match.getWinningTeamId()).getPlayers().get(0))
            .getHealth();
      }

      PracticePlayerData playerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (playerData.getPlayerSettings().isClearInventory()) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
      }
      if (playerData.getPlayerSettings().isStartFlying()) {
        player.setAllowFlight(true);
        player.setFlying(true);
      }
      playerData.setCurrentKitContents(null);
      playerData.setCurrentKitArmor(null);

      player.getActivePotionEffects().stream().map(PotionEffect::getType)
          .forEach(player::removePotionEffect);

      player.setFireTicks(0);
      player.setHealth(20.0D);
      player.setFoodLevel(20);
      player.setSaturation(12.8F);
      player.setMaximumNoDamageTicks(20);
      if (match.getKit().isBridges()) {
        SpecialMatchListener.getPlayerKills().remove(player.getUniqueId());
      }
      if (Frost.getInstance().isUsingCustomKB()) {
        PlayerManager.setKnockbackProfile(player, "default");
      }

      if (!match.getKit().getMatchEndCommands().isEmpty()) {
        match.getKit().getMatchEndCommands().forEach(command -> {
          command = command.replace("<player>", player.getName());
          plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        });
      }
    }));

    for (InventorySnapshot snapshot : match.getSnapshots().values()) {
      plugin.getManagerHandler().getInventoryManager().addSnapshot(snapshot);
    }

    Optional<Player> winnerPlayer = Optional.ofNullable(
        plugin.getServer().getPlayer(event.getWinningTeam().getLeader()));
    Optional<Player> loserPlayer = Optional.ofNullable(
        plugin.getServer().getPlayer(event.getLosingTeam().getLeader()));

    String winnerUserName = winnerPlayer.map(Player::getName).orElse("null-user-from-frost");
    String loserUserName = loserPlayer.map(Player::getName).orElse("null-user-from-frost");
    String winnerDisconnectedUserName = winnerPlayer.map(Player::getName).orElse("Disconnected");
    String loserDisconnectedUserName = loserPlayer.map(Player::getName).orElse("Disconnected");

    UUID loserUuid = loserPlayer.map(Player::getUniqueId).orElse(UUID.randomUUID());
    UUID winnerUuid = winnerPlayer.map(Player::getUniqueId).orElse(UUID.randomUUID());

    String loserMessageClick = CC.color(
        messages.getString("VIEW-INV-TEXT-LOSER").replace("<loser>", loserDisconnectedUserName));
    String winnerMessageClick = CC.color(
        messages.getString("VIEW-INV-TEXT-WINNER").replace("<winner>", winnerDisconnectedUserName));

    String winnerTitle = CC.color(
        messages.getString("WINNER-TITLE").replace("<player>", winnerDisconnectedUserName));
    String loserTitle = CC.color(
        messages.getString("LOSER-TITLE").replace("<player>", loserDisconnectedUserName));
    String matchSubtitle = CC.color(
        messages.getString("THIS-SUBTITLE").replace("<player>", winnerDisconnectedUserName));
    winnerPlayer.ifPresent(player -> player.sendTitle(
        new Title(winnerTitle, CC.color("&a" + matchSubtitle), 0, 60, 10)));
    loserPlayer.ifPresent(player -> player.sendTitle(
        new Title(loserTitle, CC.color("&c" + matchSubtitle), 0, 60, 10)));

    MatchTeam winnerTeam = event.getWinningTeam();
    MatchTeam losingTeam = event.getLosingTeam();

    if (match.getKit().isBridges() || match.getKit().isBattleRush() || match.getKit().isMlgRush()) {
      for (String bridges : messages.getStringList("BRIDGES-POST-MATCH")) {
        match.broadcast(bridges
            .replace("<scores>", winnerTeam.getTeamID() == 1 ? "&9&l"
                : "&c&l" + winnerTeam.getBridgesPoints() + "&7 - " + (winnerTeam.getTeamID() == 1
                    ? "&c&l" : "&9&l") + losingTeam.getBridgesPoints())
            .replace("<winning_team>", winnerTeam.getTeamID() == 1 ? "Blue" : "Red")
            .replace("<winning_team_color>", winnerTeam.getTeamID() == 1 ? "&9" : "&c")
            .replace("<winning_team_points>", String.valueOf(winnerTeam.getBridgesPoints()))
            .replace("<losing_team>", losingTeam.getTeamID() == 1 ? "Blue" : "Red")
            .replace("<losing_team_color>", losingTeam.getTeamID() == 1 ? "&9" : "&c")
            .replace("<losing_team_points>", String.valueOf(losingTeam.getBridgesPoints()))
        );
      }
    }

    if (match.isFFA()) {
      TextComponent loserComponent = new TextComponent("");
      TextComponent messageComponent = new TextComponent("");

      int indexLoser = 0;
      int loserSize = event.getLosingTeam().getPlayers().size();
      for (UUID player : event.getLosingTeam().getPlayers()) {
        indexLoser++;
        TextComponent playerComponent = new TextComponent(CC.color(
            messages.getString("POST-MATCH.PARTY-FFA-FORMAT.LOSER")
                .replaceAll("<player>", Bukkit.getOfflinePlayer(player).getName())));
        if (loserSize > indexLoser) {
          playerComponent.addExtra(
              CC.color(messages.getString("POST-MATCH.PARTY-FFA-FORMAT.COMMA")));
        }

        OfflinePlayer nextPlayer = Bukkit.getOfflinePlayer(player);
        if (nextPlayer.getName().equals(winnerUserName)) {
          playerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder(loserMessageClick).create()));
        } else {
          playerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder(loserMessageClick.replace(winnerDisconnectedUserName,
                  nextPlayer.getName())).create()));
        }
        playerComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            "/_ " + match.getSnapshot(player).getSnapshotId()));
        if (!nextPlayer.getName().equalsIgnoreCase(
            Bukkit.getOfflinePlayer(event.getWinningTeam().getAlivePlayers().get(0)).getName())) {
          loserComponent.addExtra(playerComponent);
        }
      }

      AtomicBoolean atomicBoolean = new AtomicBoolean(true);
      messages.getStringList("POST-MATCH.PARTY-FFA").forEach(s -> {
        String string = CC.color(s.replaceAll("<winner>",
            Bukkit.getPlayer(event.getWinningTeam().getAlivePlayers().get(0)).getName()));
        TextComponent textComponent = new TextComponent("");

        if (string.contains("<loser_members>")) {
          String[] args = string.split("<loser_members>");
          if (args.length == 1) {
            textComponent.addExtra(args[0]);
            textComponent.addExtra(loserComponent);
          } else {
            textComponent.addExtra(args[0]);
            textComponent.addExtra(loserComponent);
            textComponent.addExtra(args[1]);
          }
        } else {
          textComponent.addExtra(string);
        }

        if (atomicBoolean.get()) {
          atomicBoolean.set(false);
        } else {
          messageComponent.addExtra("\n");
        }
        messageComponent.addExtra(textComponent);
      });

      match.broadcast(messageComponent);
    } else if (match.isParty() || match.isPartyMatch()) {
      TextComponent winComponent = new TextComponent("");
      TextComponent loserComponent = new TextComponent("");
      TextComponent messageComponent = new TextComponent("");

      int indexWinner = 0;
      int winnerSize = winnerTeam.getPlayers().size();
      for (UUID player : winnerTeam.getPlayers()) {
        indexWinner++;
        TextComponent playerComponent = new TextComponent(CC.color(
            messages.getString("POST-MATCH.PARTY-SPLIT-FORMAT.WIN")
                .replaceAll("<player>", Bukkit.getOfflinePlayer(player).getName())));
        if (winnerSize > indexWinner) {
          playerComponent.addExtra(
              CC.color(messages.getString("POST-MATCH.PARTY-SPLIT-FORMAT.COMMA")));
        }

        OfflinePlayer nextPlayer = Bukkit.getOfflinePlayer(player);
        if (nextPlayer.getName().equals(winnerUserName)) {
          playerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder(winnerMessageClick).create()));
        } else {
          playerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder(winnerMessageClick.replace(winnerDisconnectedUserName,
                  nextPlayer.getName())).create()));
        }
        playerComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            "/_ " + match.getSnapshot(player).getSnapshotId()));
        winComponent.addExtra(playerComponent);
      }

      int indexLoser = 0;
      int loserSize = losingTeam.getPlayers().size();
      for (UUID player : losingTeam.getPlayers()) {
        indexLoser++;
        TextComponent playerComponent = new TextComponent(CC.color(
            messages.getString("POST-MATCH.PARTY-SPLIT-FORMAT.LOSER")
                .replaceAll("<player>", Bukkit.getOfflinePlayer(player).getName())));
        if (loserSize > indexLoser) {
          playerComponent.addExtra(
              CC.color(messages.getString("POST-MATCH.PARTY-SPLIT-FORMAT.COMMA")));
        }

        OfflinePlayer nextPlayer = Bukkit.getOfflinePlayer(player);
        if (nextPlayer.getName().equals(winnerUserName)) {
          playerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder(loserMessageClick).create()));
        } else {
          playerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder(
                  loserMessageClick.replace(winnerUserName, nextPlayer.getName())).create()));
        }
        playerComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            "/_ " + match.getSnapshot(player).getSnapshotId()));
        loserComponent.addExtra(playerComponent);
      }

      AtomicBoolean atomicBoolean = new AtomicBoolean(true);
      messages.getStringList("POST-MATCH.PARTY-SPLIT").forEach(s -> {
        String string = CC.color(s
            .replace("<winner>", winnerTeam.getLeaderName())
            .replace("<loser>", losingTeam.getLeaderName())
        );

        TextComponent textComponent = new TextComponent("");
        if (string.contains("<winner_members>")) {
          String[] args = string.split("<winner_members>");
          if (args.length == 1) {
            textComponent.addExtra(args[0]);
            textComponent.addExtra(winComponent);
          } else {
            textComponent.addExtra(args[0]);
            textComponent.addExtra(winComponent);
            textComponent.addExtra(args[1]);
          }
        } else if (string.contains("<loser_members>")) {
          String[] args = string.split("<loser_members>");
          if (args.length == 1) {
            textComponent.addExtra(args[0]);
            textComponent.addExtra(loserComponent);
          } else {
            textComponent.addExtra(args[0]);
            textComponent.addExtra(loserComponent);
            textComponent.addExtra(args[1]);
          }
        } else {
          textComponent.addExtra(string);
        }

        if (atomicBoolean.get()) {
          atomicBoolean.set(false);
        } else {
          messageComponent.addExtra("\n");
        }
        messageComponent.addExtra(textComponent);
      });

      match.broadcast(messageComponent);
    } else {
      messages.getStringList("POST-MATCH.NORMAL").forEach(text -> {
        if (text.contains("<winner>") || text.contains("<loser>")) {
          TextComponent textComponent = new TextComponent();
          textComponent.setText(CC.color(text)
              .replace("<winner>", winnerUserName)
              .replace("<winnerHealth>", String.valueOf(MathUtil.roundToHalves(health / 2.0D)))
              .replace("<loser>", loserUserName));
          textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
              "/_ " + (text.contains("<winner>") ? match.getSnapshot(winnerUuid).getSnapshotId()
                  : match.getSnapshot(loserUuid).getSnapshotId())));
          textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder(
                  text.contains("<winner>") ? winnerMessageClick : loserMessageClick).create()));
          match.broadcast(textComponent);
        } else {
          match.broadcast(text);
        }
      });

      List<UUID> factualMfSpecs = new ArrayList<>();
      for (UUID spectator : spectators) {
        if (spectator != loserUuid) {
          if (!plugin.getManagerHandler().getPlayerManager().getPlayerData(spectator).isSilent()) {
            factualMfSpecs.add(spectator);
          }
        }
      }

      if (factualMfSpecs.size() >= 1) {
        match.broadcast(messages.getString("POST-MATCH.SPECTATORS")
            .replace("<watching>", String.valueOf(factualMfSpecs.size()))
            .replace("<spectators>", factualMfSpecs.stream()
                .map(uuid -> Bukkit.getPlayer(uuid).getName())
                .collect(Collectors.joining(", "))
            )
        );
      }

      Player winnerLeader = plugin.getServer()
          .getPlayer(event.getWinningTeam().getPlayers().get(0));
      PracticePlayerData winnerLeaderData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(winnerLeader.getUniqueId());
      Player loserLeader = plugin.getServer().getPlayer(event.getLosingTeam().getPlayers().get(0));
      PracticePlayerData loserLeaderData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(loserLeader.getUniqueId());

      winnerLeaderData.setMatchesPlayed(winnerLeaderData.getMatchesPlayed() + 1);
      loserLeaderData.setMatchesPlayed(loserLeaderData.getMatchesPlayed() + 1);

      String kitName = match.getKit().getName();

      winnerLeaderData.setCurrentWinstreak(kitName,
          winnerLeaderData.getCurrentWinstreak(kitName) + 1);
      loserLeaderData.setCurrentWinstreak(kitName, 0);
      if (winnerLeaderData.getCurrentWinstreak(kitName) > winnerLeaderData.getHighestWinStreak(
          kitName)) {
        winnerLeaderData.setHighestWinStreak(kitName,
            winnerLeaderData.getCurrentWinstreak(kitName));
      }

      winnerLeaderData.setGlobalWinStreak(winnerLeaderData.getGlobalWinStreak() + 1);
      loserLeaderData.setGlobalWinStreak(0);
      if (winnerLeaderData.getGlobalWinStreak() > winnerLeaderData.getGlobalHighestWinStreak()) {
        winnerLeaderData.setGlobalHighestWinStreak(winnerLeaderData.getGlobalWinStreak());
      }

      if (match.getType().isRanked()) {
        String eloMessage;

        int[] preElo = new int[2];
        int[] newElo = new int[2];
        int winnerElo;
        int loserElo;
        int newWinnerElo;
        int newLoserElo;

        winnerElo = winnerLeaderData.getElo(kitName);
        loserElo = loserLeaderData.getElo(kitName);

        preElo[0] = winnerElo;
        preElo[1] = loserElo;

        newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
        newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);

        newElo[0] = newWinnerElo;
        newElo[1] = newLoserElo;

        eloMessage = CC.color(messages.getString("POST-MATCH.RANKED")
            .replace("<winner>", winnerLeader.getName())
            .replace("<loser>", loserLeader.getName())
            .replace("<newWinnerElo>", String.valueOf(newWinnerElo))
            .replace("<newLoserElo>", String.valueOf(newLoserElo))
            .replace("<winnerElo>", String.valueOf(newWinnerElo - winnerElo))
            .replace("<loserElo>", String.valueOf(newLoserElo - loserElo))
        );

        if (match.getType() == QueueType.RANKED) {
          winnerLeaderData.setElo(kitName, newWinnerElo);
          loserLeaderData.setElo(kitName, newLoserElo);

          winnerLeaderData.setWins(kitName, winnerLeaderData.getWins(kitName) + 1);
          loserLeaderData.setLosses(kitName, loserLeaderData.getLosses(kitName) + 1);
        }

        if (match.getType().isPremium()) {
          winnerElo = winnerLeaderData.getPremiumElo();
          loserElo = loserLeaderData.getPremiumElo();

          preElo[0] = winnerElo;
          preElo[1] = loserElo;

          newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
          newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);

          newElo[0] = newWinnerElo;
          newElo[1] = newLoserElo;

          eloMessage = ChatColor.AQUA + "Premium Elo Changes: " +
              ChatColor.GREEN + winnerLeader.getName() + " +" + (newWinnerElo - winnerElo) + " ("
              + newWinnerElo + ") " +
              ChatColor.RED + loserLeader.getName() + " " + (newLoserElo - loserElo) + " ("
              + newLoserElo + ")";

          winnerLeaderData.setPremiumElo(newWinnerElo);
          loserLeaderData.setPremiumElo(newLoserElo);
          winnerLeaderData.setPremiumMatches(winnerLeaderData.getPremiumMatches() - 1);
          loserLeaderData.setPremiumMatches(loserLeaderData.getPremiumMatches() - 1);
        }

        match.broadcast(eloMessage);

        if (RatingUtil.isPromotionGame(winnerElo, newWinnerElo)) {
          for (String info : messages.getStringList("RANKED-UP")) {
            Bukkit.broadcastMessage(CC.color(info)
                .replace("<player>", winnerDisconnectedUserName)
                .replace("<old_rating>", RatingUtil.getRankByElo(winnerElo).getName())
                .replace("<new_rating>", RatingUtil.getRankByElo(newWinnerElo).getName())
                .replace("<kit_name>", kitName)
            );
          }
        }

        MatchLocatedData matchLocatedData = new MatchLocatedData();
        matchLocatedData.setId(UUID.randomUUID().toString().split("-")[0]);
        matchLocatedData.setWinnerUUID(winnerUuid);
        matchLocatedData.setLoserUUID(loserUuid);
        matchLocatedData.setWinnerEloModifier(newWinnerElo - winnerElo);
        matchLocatedData.setLoserEloModifier(newLoserElo - loserElo);
        matchLocatedData.setWinnerElo(newWinnerElo);
        matchLocatedData.setLoserElo(newLoserElo);
        matchLocatedData.setDate(TimeUtils.nowDate());
        matchLocatedData.setKit(match.getKit().getName());
        matchLocatedData.setWinnerArmor(
            event.getMatch().getSnapshot(winnerUuid).getOriginalArmor());
        matchLocatedData.setWinnerContents(
            event.getMatch().getSnapshot(winnerUuid).getOriginalInventory());
        matchLocatedData.setLoserArmor(event.getMatch().getSnapshot(loserUuid).getOriginalArmor());
        matchLocatedData.setLoserContents(
            event.getMatch().getSnapshot(loserUuid).getOriginalInventory());

        matchLocatedData.save();
      }

      plugin.getManagerHandler().getMatchManager().savePostMatch(match);
      plugin.getManagerHandler().getMatchManager().processRequeue(loserLeader, match);
      plugin.getManagerHandler().getMatchManager().processRequeue(winnerLeader, match);
    }
  }
}
