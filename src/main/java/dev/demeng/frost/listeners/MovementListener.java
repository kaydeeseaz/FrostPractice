package dev.demeng.frost.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.oitc.OITCEvent;
import dev.demeng.frost.events.games.oitc.OITCPlayer;
import dev.demeng.frost.events.games.stoplight.StopLightEvent;
import dev.demeng.frost.events.games.stoplight.StopLightPlayer;
import dev.demeng.frost.events.games.sumo.SumoEvent;
import dev.demeng.frost.events.games.sumo.SumoPlayer;
import dev.demeng.frost.game.event.match.MatchEndEvent;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.runnable.CustomMatchRespawnRunnable;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.timer.impl.BridgeArrowTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MovementListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (event.getPlayer() == null) {
      return;
    }

    Location to = event.getTo();
    Location from = event.getFrom();
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData == null) {
      plugin.getLogger().warning(
          player.getName() + "'s player data is null" + "(" + this.getClass().getName() + ")");
      return;
    }

    if (practicePlayerData.getPlayerState() == PlayerState.FIGHTING) {
      Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
      if (match == null) {
        return;
      }

      if (match.getKit().isSumo() || match.getKit().isSpleef() || match.getKit().isBridges()
          || match.getKit().isSkyWars() || match.getKit().isBedWars() || match.getKit()
          .isBattleRush() || match.getKit().isStickFight() || match.getKit().isMlgRush()) {
        if (match.getKit().isSumo() || match.getKit().isSpleef()) {
          if (to.getBlock().isLiquid()) {
            plugin.getManagerHandler().getMatchManager()
                .removeFighter(player, practicePlayerData, true);
          }
        }

        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
          if (match.getMatchState() == MatchState.STARTING) {
            player.teleport(from);
          }
        }

        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
          return;
        }

        if (match.getKit().isBridges()) {
          MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
          if (event.getFrom().getBlockY() <= match.getArena().getDeadZone()) {
            player.teleport(playerTeam.getBridgeSpawnLocation());
            match.broadcast(
                (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName() + " &7has died.");
            match.broadcastSound(Sound.ORB_PICKUP);

            match.getKit().applyKit(player, practicePlayerData);

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
          }
        }

        if (match.getKit().isBedWars()) {
          MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
          if (event.getFrom().getBlockY() <= match.getArena().getDeadZone()
              && !player.hasPotionEffect(PotionEffectType.WEAKNESS)
              && match.getMatchState() != MatchState.ENDING) {
            if (playerTeam.isHasBed()) {
              match.broadcast(
                  (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName() + " &7has died.");
              match.broadcastSound(Sound.ORB_PICKUP);
              new CustomMatchRespawnRunnable(player, practicePlayerData, match, playerTeam, 4,
                  4).runTaskTimer(plugin, 0L, 20L);
            } else if (match.isPartyMatch()) {
              if (!playerTeam.isHasBed()) {
                plugin.getManagerHandler().getMatchManager()
                    .removeFighter(player, practicePlayerData, true);
              }
            } else if (match.isEnding()) {
              player.teleport(plugin.getManagerHandler().getSpawnManager().getSpawnLocation()
                  .toBukkitLocation());
            } else {
              match.setCanContinue(false);
              if (!match.isCanContinue() && !match.isEnding()) {
                MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
                    : ((practicePlayerData.getTeamId() == 0) ? match.getTeams().get(1)
                        : match.getTeams().get(0));
                Bukkit.getPluginManager()
                    .callEvent(new MatchEndEvent(match, opposingTeam, playerTeam));
                player.teleport(playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA()
                    .toBukkitLocation() : match.getStandaloneArena().getB().toBukkitLocation());
                match.broadcastSound(Sound.FIREWORK_LAUNCH);
                match.broadcastSound(Sound.FIREWORK_TWINKLE2);
              }
            }
          }
        }

        if (match.getKit().isBattleRush()) {
          MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
          if (event.getFrom().getBlockY() <= match.getArena().getDeadZone()
              && !player.hasPotionEffect(PotionEffectType.WEAKNESS)
              && match.getMatchState() != MatchState.ENDING) {
            match.broadcast(
                (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName() + " &7has died.");
            match.broadcastSound(Sound.ORB_PICKUP);
            new CustomMatchRespawnRunnable(player, practicePlayerData, match, playerTeam, 3,
                3).runTaskTimer(plugin, 0L, 20L);
          }
        }

        if (match.getKit().isStickFight()) {
          MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
          if (event.getFrom().getBlockY() <= match.getArena().getDeadZone()
              && !player.hasPotionEffect(PotionEffectType.WEAKNESS)
              && match.getMatchState() != MatchState.ENDING) {
            playerTeam.removeLife();
            match.broadcast(
                (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName() + " &7has died.");
            if (playerTeam.getLives() < 1) {
              match.setCanContinue(false);
              if (!match.isCanContinue() && !match.isEnding()) {
                MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
                    : ((practicePlayerData.getTeamId() == 0) ? match.getTeams().get(1)
                        : match.getTeams().get(0));
                Bukkit.getPluginManager()
                    .callEvent(new MatchEndEvent(match, opposingTeam, playerTeam));
                player.teleport(playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA()
                    .toBukkitLocation() : match.getStandaloneArena().getB().toBukkitLocation());
                match.broadcastSound(Sound.FIREWORK_LAUNCH);
                match.broadcastSound(Sound.FIREWORK_TWINKLE2);
                return;
              }
            }

            match.broadcastSound(Sound.ORB_PICKUP);
            new CustomMatchRespawnRunnable(player, practicePlayerData, match, playerTeam, 4,
                4).runTaskTimer(plugin, 0L, 20L);
          }
        }

        if (match.getKit().isMlgRush()) {
          MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
          if (event.getFrom().getBlockY() <= match.getArena().getDeadZone()) {
            player.teleport(
                playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                    : match.getStandaloneArena().getB().toBukkitLocation());
            match.broadcast(
                (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName() + " &7has died.");
            match.broadcastSound(Sound.ORB_PICKUP);

            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 0));
            player.resetMaxHealth();
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);

            match.getKit().applyKit(player, practicePlayerData);

            return;
          }
        }
      }
    }

    PracticeEvent<?> pEvent = plugin.getManagerHandler().getEventManager().getEventPlaying(player);
    if (pEvent != null) {
      if (pEvent instanceof SumoEvent) {
        SumoEvent sumoEvent = (SumoEvent) pEvent;
        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
          if (sumoEvent.getPlayer(player).getFighting() != null
              && sumoEvent.getPlayer(player).getState() == SumoPlayer.SumoState.PREPARING) {
            player.teleport(from);
          }
        }
      } else if (pEvent instanceof OITCEvent) {
        OITCEvent oitcEvent = (OITCEvent) pEvent;
        if (oitcEvent.getPlayer(player).getState() == OITCPlayer.OITCState.RESPAWNING) {
          player.teleport(from);
        }
      } else if (pEvent instanceof StopLightEvent) {
        StopLightEvent lightsEvent = (StopLightEvent) pEvent;
        if (lightsEvent.getPlayer(player) != null
            && lightsEvent.getPlayer(player).getState() == StopLightPlayer.State.INGAME
            && lightsEvent.getCurrent() == StopLightEvent.State.STOP) {
          if (from.distance(to) >= 0.3 && !lightsEvent.getMovingPlayers()
              .contains(player.getUniqueId())) {
            lightsEvent.getMovingPlayers().add(player.getUniqueId());
          }
        }
        if (lightsEvent.getPlayer(player) != null
            && lightsEvent.getPlayer(player).getState() == StopLightPlayer.State.INGAME
            && PlayerUtil.isStandingOn(player, Material.GOLD_PLATE)) {
          lightsEvent.handleWin(player);
          lightsEvent.end();
          lightsEvent.getGameTask().cancel();
        }
      }
    }
  }

  @EventHandler
  public void onFlight(PlayerToggleFlightEvent event) {
    if (event.getPlayer().hasPotionEffect(PotionEffectType.WEAKNESS)) {
      event.setCancelled(true);
    }
  }
}
