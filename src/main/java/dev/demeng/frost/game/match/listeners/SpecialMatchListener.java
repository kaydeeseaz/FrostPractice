package dev.demeng.frost.game.match.listeners;

import static dev.demeng.frost.util.CC.color;
import static dev.demeng.frost.util.CC.sendTitle;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.event.match.MatchEndEvent;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.timer.impl.BridgeArrowTimer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpecialMatchListener implements Listener {

  @Getter public static final Map<UUID, Integer> playerKills = new HashMap<>();
  private final Frost plugin = Frost.getInstance();

  public static boolean isOnBridge(Player player) {
    PracticePlayerData practicePlayerData = Frost.getInstance().getManagerHandler()
        .getPlayerManager().getPlayerData(player.getUniqueId());
    if (!practicePlayerData.isInMatch()) {
      return false;
    }

    return Frost.getInstance().getManagerHandler().getMatchManager().getMatch(practicePlayerData)
        .getKit().isBridges();
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    if (player == null) {
      return;
    }

    Player killer = event.getEntity().getKiller();
    PracticePlayerData playerData = Frost.getInstance().getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (playerData.isInEvent() || !playerData.isInMatch()) {
      return;
    }

    Match match = Frost.getInstance().getManagerHandler().getMatchManager().getMatch(playerData);
    if (match == null) {
      return;
    }

    Kit kit = match.getKit();
    if (kit.isBridges()) {
      if (killer != null) {
        if (playerKills.containsKey(killer.getUniqueId())) {
          playerKills.put(killer.getUniqueId(), playerKills.get(killer.getUniqueId()) + 1);
        } else {
          playerKills.put(killer.getUniqueId(), 1);
        }
      } else {
        return;
      }

      MatchTeam playerTeam = match.getTeams().get(playerData.getTeamId());
      match.broadcast(
          (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName() + "&7 was killed by " + (
              playerTeam.getTeamID() == 1 ? "&c" : "&9") + killer.getName() + "&7.");
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        plugin.getManagerHandler().getTimerManager().getTimer(BridgeArrowTimer.class)
            .clearCooldown(player.getUniqueId());

        player.resetMaxHealth();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        kit.applyKit(player, playerData);

        Board board = Board.getByPlayer(player);
        if (board == null) {
          return;
        }

        BoardCooldown cooldown = board.getCooldown("arrow");
        if (cooldown != null) {
          cooldown.cancel();
          player.setLevel(0);
          player.setExp(0.0F);
        }
      }, 1L);
    }

    if (kit.isBedWars()) {
      MatchTeam playerTeam = match.getTeams().get(playerData.getTeamId());
      if (playerTeam.isHasBed() && !match.isEnding()) {
        if (killer != null) {
          match.broadcast(
              (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName() + "&7 was killed by "
                  + (playerTeam.getTeamID() == 1 ? "&c" : "&9") + killer.getName() + "&7.");
        }

        PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0);
        new BukkitRunnable() {
          int respawn = 4;

          @Override
          public void run() {
            if (respawn <= 1) {
              player.removePotionEffect(PotionEffectType.WEAKNESS);
              player.teleport(
                  playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                      : match.getStandaloneArena().getB().toBukkitLocation());

              match.getTeams().forEach(
                  team -> team.alivePlayers().filter(player1 -> !player.equals(player1)).forEach(
                      matchplayer -> matchplayer.sendMessage(CC.color(
                          (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName()
                              + "&a has respawned!"))));
              match.getTeams().forEach(team -> team.alivePlayers()
                  .forEach(matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, player, false)));

              player.setFallDistance(50);
              player.setAllowFlight(false);
              player.setFlying(false);

              player.setHealth(player.getMaxHealth());
              player.setFoodLevel(20);

              player.sendMessage(CC.color("&aYou have respawned!"));
              player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 1);

              Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.resetMaxHealth();
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);

                kit.applyKit(player, playerData);
                sendTitle(player, "&aRespawning...", "");
                cancel();
              }, 2L);
            }

            if (respawn == 4) {
              player.addPotionEffect(weakness);

              match.getTeams().forEach(team -> team.alivePlayers()
                  .forEach(matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, player, true)));

              player.getInventory().clear();
              player.getInventory().setArmorContents(null);
              player.updateInventory();

              player.setHealth(player.getMaxHealth());
              player.setFoodLevel(20);

              player.setVelocity(player.getVelocity().add(new org.bukkit.util.Vector(0, 0.25, 0)));
              player.setAllowFlight(true);
              player.setFlying(true);
              player.setVelocity(player.getVelocity().add(new Vector(0, 0.15, 0)));
              player.setAllowFlight(true);
              player.setFlying(true);

              if (killer != null) {
                player.teleport(killer.getLocation());
              }
            }

            respawn--;
            sendTitle(player, "&a" + respawn, "");
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1);
          }
        }.runTaskTimer(plugin, 0L, 20L);
      } else if (match.isPartyMatch()) {
        plugin.getManagerHandler().getMatchManager().removeFighter(player, playerData, true);
      } else if (match.isEnding()) {
        player.teleport(
            plugin.getManagerHandler().getSpawnManager().getSpawnLocation().toBukkitLocation());
      } else {
        match.setCanContinue(false);
        if (!match.isCanContinue() && !match.isEnding()) {
          MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
              : ((playerData.getTeamId() == 0) ? match.getTeams().get(1) : match.getTeams().get(0));
          Bukkit.getPluginManager().callEvent(new MatchEndEvent(match, opposingTeam, playerTeam));
          player.teleport(
              playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                  : match.getStandaloneArena().getB().toBukkitLocation());
          match.broadcastSound(Sound.FIREWORK_LAUNCH);
          match.broadcastSound(Sound.FIREWORK_TWINKLE2);
        }
      }
    }
  }

  @EventHandler
  public void onPortalEntry(EntityPortalEnterEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    ConfigCursor matchMessage = new ConfigCursor(Frost.getInstance().getMessagesConfig(),
        "MESSAGES.MATCH");
    if (practicePlayerData.getPlayerState() == PlayerState.FIGHTING) {
      Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
      if (match.getMatchState() == MatchState.ENDING) {
        return;
      }

      if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
        return;
      }

      Kit kit = match.getKit();
      if (kit.isBridges()) {
        MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
        if (playerTeam.getBridgeSpawnLocation().distance(event.getLocation()) < 27.0) {
          if (player.getLocation().distance(playerTeam.getBridgeSpawnLocation()) < 4) {
            return;
          }

          player.teleport(playerTeam.getBridgeSpawnLocation());

          kit.applyKit(player, practicePlayerData);

          player.resetMaxHealth();
          player.setHealth(player.getMaxHealth());
          player.setFoodLevel(20);
          player.removePotionEffect(PotionEffectType.ABSORPTION);

          plugin.getManagerHandler().getTimerManager().getTimer(BridgeArrowTimer.class)
              .clearCooldown(player.getUniqueId());

          Board board = Board.getByPlayer(player);
          if (board == null) {
            return;
          }

          BoardCooldown cooldown = board.getCooldown("arrow");
          if (cooldown != null) {
            cooldown.cancel();
            player.setLevel(0);
            player.setExp(0.0F);
          }
          return;
        }

        if (playerTeam.getPlayers().contains(player.getUniqueId()) && playerTeam.isAbleToScore()) {
          playerTeam.addPoint();
          MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
              : ((practicePlayerData.getTeamId() == 0) ? match.getTeams().get(1)
                  : match.getTeams().get(0));
          if (playerTeam.getBridgesPoints() >= 5) {
            match.setCanContinue(false);
            if (!match.isCanContinue()) {
              Bukkit.getPluginManager()
                  .callEvent(new MatchEndEvent(match, playerTeam, opposingTeam));
              player.teleport(playerTeam.getBridgeSpawnLocation());
            }

            return;
          }

          match.setCountdown(6);
          player.teleport(playerTeam.getBridgeSpawnLocation());
          match.broadcast(
              playerTeam.getTeamID() == 1 ? "&9" : "&c" + player.getName() + " &ascored a point!");

          new BukkitRunnable() {
            @Override
            public void run() {
              if (match.getCountdown() <= 1) {
                match.getTeams().forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
                  Player playerBukkitTeam = Bukkit.getPlayer(uuid);
                  if (playerBukkitTeam == null) {
                    return;
                  }

                  playerBukkitTeam.setMaxHealth(playerBukkitTeam.getMaxHealth());
                  playerBukkitTeam.setFoodLevel(20);
                }));

                match.broadcastSound(Sound.FIREWORK_BLAST);
                cancel();

                return;
              }

              if (match.getCountdown() == 6) {
                match.getTeams().forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
                  Player bridgePlayer = Bukkit.getPlayer(uuid);
                  if (bridgePlayer == null) {
                    return;
                  }

                  PlayerUtil.lockPos(bridgePlayer, 5);
                  bridgePlayer.teleport(matchTeam.getBridgeSpawnLocation());
                  plugin.getManagerHandler().getTimerManager().getTimer(BridgeArrowTimer.class)
                      .clearCooldown(bridgePlayer.getUniqueId());

                  Board board = Board.getByPlayer(bridgePlayer);
                  if (board == null) {
                    return;
                  }
                  BoardCooldown cooldown = board.getCooldown("arrow");
                  if (cooldown != null) {
                    cooldown.cancel();
                    bridgePlayer.setLevel(0);
                    bridgePlayer.setExp(0.0F);
                  }
                }));
              }

              match.setCountdown(match.getCountdown() - 1);
              match.broadcastTitle(
                  matchMessage.getString("BRIDGE-COUNTDOWN-TITLE")
                      .replace("<team_color>", playerTeam.getTeamID() == 1 ? "&9" : "&c")
                      .replace("<match_countdown>", String.valueOf(match.getCountdown()))
                      .replace("<player>", player.getName()),
                  matchMessage.getString("BRIDGE-COUNTDOWN-SUBTITLE")
                      .replace("<match_countdown>", String.valueOf(match.getCountdown()))
                      .replace("<scores>", (playerTeam.getTeamID() == 1 ? "&9" : "&c")
                          + playerTeam.getBridgesPoints() + " &7- " + (opposingTeam.getTeamID() == 1
                          ? "&9" : "&c") + opposingTeam.getBridgesPoints())
              );
              match.broadcastWithSound(matchMessage.getString("BRIDGE-COUNTDOWN")
                      .replace("<match_countdown>", String.valueOf(match.getCountdown())),
                  Sound.NOTE_PLING);
            }
          }.runTaskTimer(plugin, 0L, 20L);

          match.getTeams().forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
            Player playerBukkitTeam = Bukkit.getPlayer(uuid);
            if (playerBukkitTeam == null) {
              return;
            }

            PracticePlayerData dataBukkitTeam = plugin.getManagerHandler().getPlayerManager()
                .getPlayerData(playerBukkitTeam.getUniqueId());
            kit.applyKit(playerBukkitTeam, dataBukkitTeam);

            playerBukkitTeam.updateInventory();
            playerBukkitTeam.resetMaxHealth();
            playerBukkitTeam.setHealth(playerBukkitTeam.getMaxHealth());
            playerBukkitTeam.setFoodLevel(20);
            playerBukkitTeam.removePotionEffect(PotionEffectType.ABSORPTION);

            plugin.getManagerHandler().getTimerManager().getTimer(BridgeArrowTimer.class)
                .clearCooldown(playerBukkitTeam.getUniqueId());

            Board board = Board.getByPlayer(playerBukkitTeam);
            if (board == null) {
              return;
            }

            BoardCooldown cooldown = board.getCooldown("arrow");
            if (cooldown != null) {
              cooldown.cancel();
              playerBukkitTeam.setLevel(0);
              playerBukkitTeam.setExp(0.0F);
            }
          }));
        }
        return;
      }

      if (kit.isBattleRush()) {
        MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
        if ((playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
            : match.getStandaloneArena().getB().toBukkitLocation()).distance(event.getLocation())
            < 27.0) {
          if (player.getLocation().distance(
              playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                  : match.getStandaloneArena().getB().toBukkitLocation()) < 4) {
            return;
          }

          player.teleport(
              playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                  : match.getStandaloneArena().getB().toBukkitLocation());
          PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0);
          match.broadcastSound(Sound.ORB_PICKUP);

          new BukkitRunnable() {
            int respawn = 3;

            @Override
            public void run() {
              if (respawn <= 1) {
                player.removePotionEffect(PotionEffectType.WEAKNESS);
                player.teleport(playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA()
                    .toBukkitLocation() : match.getStandaloneArena().getB().toBukkitLocation());

                match.getTeams().forEach(team -> team.alivePlayers().forEach(
                    matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, player, false)));

                player.setFallDistance(15);
                player.setAllowFlight(false);
                player.setFlying(false);

                color("&aYou have respawned!");
                sendTitle(player, "&aRespawning...", "");
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);

                match.getTeams().forEach(team -> team.alivePlayers()
                    .filter(filteredPlayer -> !player.equals(filteredPlayer)).forEach(
                        matchplayer -> matchplayer.sendMessage(CC.color(
                            playerTeam.getTeamID() == 1 ? "&9"
                                : "&c" + player.getName() + "&a has respawned!"))));

                kit.applyKit(player, practicePlayerData);

                player.resetMaxHealth();
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);

                cancel();
                return;
              }

              if (respawn == 3) {
                player.addPotionEffect(weakness);

                match.getTeams().forEach(team -> team.alivePlayers().forEach(
                    matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, player, true)));

                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.updateInventory();

                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);

                player.setVelocity(player.getVelocity().add(new Vector(0, 0.25, 0)));
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setVelocity(player.getVelocity().add(new Vector(0, 0.15, 0)));
                player.setAllowFlight(true);
                player.setFlying(true);
              }

              respawn = respawn - 1;
              sendTitle(player, "&a" + respawn, "");
              player.playSound(player.getLocation(), Sound.NOTE_PLING, 10, 1);
            }
          }.runTaskTimer(plugin, 0L, 20L);

          return;
        }

        if (playerTeam.getPlayers().contains(player.getUniqueId()) && playerTeam.isAbleToScore()) {
          playerTeam.addPoint();
          MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
              : ((practicePlayerData.getTeamId() == 0) ? match.getTeams().get(1)
                  : match.getTeams().get(0));
          if (playerTeam.getBridgesPoints() >= 3) {
            match.setCanContinue(false);
            if (!match.isCanContinue()) {
              Bukkit.getPluginManager()
                  .callEvent(new MatchEndEvent(match, playerTeam, opposingTeam));
            }

            return;
          }

          match.setCountdown(4);
          plugin.getManagerHandler().getMatchManager().clearBlocks(match);
          match.broadcast(
              playerTeam.getTeamID() == 1 ? "&9" : "&c" + player.getName() + " &ascored a point!");
          player.teleport(
              playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                  : match.getStandaloneArena().getB().toBukkitLocation());

          new BukkitRunnable() {
            @Override
            public void run() {
              if (match.getCountdown() <= 1) {
                match.getTeams().forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
                  Player playerBukkitTeam = Bukkit.getPlayer(uuid);
                  if (playerBukkitTeam == null) {
                    return;
                  }

                  playerBukkitTeam.setMaxHealth(playerBukkitTeam.getMaxHealth());
                  playerBukkitTeam.setFoodLevel(20);
                }));

                match.broadcastSound(Sound.FIREWORK_BLAST);
                cancel();

                return;
              }

              if (match.getCountdown() == 4) {
                match.getTeams().forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
                  Player playerBukkitTeam = Bukkit.getPlayer(uuid);
                  if (playerBukkitTeam == null) {
                    return;
                  }

                  PracticePlayerData bukkitData = plugin.getManagerHandler().getPlayerManager()
                      .getPlayerData(playerBukkitTeam.getUniqueId());
                  if (bukkitData == null) {
                    return;
                  }
                  playerBukkitTeam.teleport(
                      matchTeam.getTeamID() == 1 ? match.getStandaloneArena().getA()
                          .toBukkitLocation()
                          : match.getStandaloneArena().getB().toBukkitLocation());
                  kit.applyKit(playerBukkitTeam, bukkitData);
                  PlayerUtil.lockPos(playerBukkitTeam, 3);
                }));
              }

              match.setCountdown(match.getCountdown() - 1);
              match.broadcastTitle(
                  matchMessage.getString("BRIDGE-COUNTDOWN-TITLE")
                      .replace("<team_color>", playerTeam.getTeamID() == 1 ? "&9" : "&c")
                      .replace("<match_countdown>", String.valueOf(match.getCountdown()))
                      .replace("<player>", player.getName()),
                  matchMessage.getString("BRIDGE-COUNTDOWN-SUBTITLE")
                      .replace("<match_countdown>", String.valueOf(match.getCountdown()))
                      .replace("<scores>", (playerTeam.getTeamID() == 1 ? "&9" : "&c")
                          + playerTeam.getBridgesPoints() + " &7- " + (opposingTeam.getTeamID() == 1
                          ? "&9" : "&c") + opposingTeam.getBridgesPoints())

              );
              match.broadcastWithSound(matchMessage.getString("BRIDGE-COUNTDOWN")
                      .replace("<match_countdown>", String.valueOf(match.getCountdown())),
                  Sound.NOTE_PLING);
            }
          }.runTaskTimer(plugin, 0L, 20L);
        }
      }
    }
  }
}
