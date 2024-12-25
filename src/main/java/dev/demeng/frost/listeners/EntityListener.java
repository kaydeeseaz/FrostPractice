package dev.demeng.frost.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.brackets.BracketsEvent;
import dev.demeng.frost.events.games.brackets.BracketsPlayer;
import dev.demeng.frost.events.games.corners.FourCornersEvent;
import dev.demeng.frost.events.games.dropper.DropperEvent;
import dev.demeng.frost.events.games.gulag.GulagEvent;
import dev.demeng.frost.events.games.gulag.GulagPlayer;
import dev.demeng.frost.events.games.knockout.KnockoutEvent;
import dev.demeng.frost.events.games.knockout.KnockoutPlayer;
import dev.demeng.frost.events.games.lms.LMSEvent;
import dev.demeng.frost.events.games.lms.LMSPlayer;
import dev.demeng.frost.events.games.oitc.OITCEvent;
import dev.demeng.frost.events.games.oitc.OITCPlayer;
import dev.demeng.frost.events.games.parkour.ParkourEvent;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.events.games.skywars.SkyWarsPlayer;
import dev.demeng.frost.events.games.spleef.SpleefEvent;
import dev.demeng.frost.events.games.stoplight.StopLightEvent;
import dev.demeng.frost.events.games.sumo.SumoEvent;
import dev.demeng.frost.events.games.sumo.SumoPlayer;
import dev.demeng.frost.events.games.thimble.ThimbleEvent;
import dev.demeng.frost.events.games.tnttag.TNTTagEvent;
import dev.demeng.frost.events.games.tnttag.TNTTagPlayer;
import dev.demeng.frost.game.event.match.MatchEndEvent;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.match.listeners.SpecialMatchListener;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import dev.demeng.frost.user.effects.SpecialEffects;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.timer.impl.BridgeArrowTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EntityListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler
  public void onEntityDamage(EntityDamageEvent e) {
    if (e.getEntity().hasMetadata("NPC")) {
      e.setCancelled(true);
      return;
    }
    if (e.getEntity() instanceof Player) {
      Player player = (Player) e.getEntity();
      if (player == null) {
        return;
      }

      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      switch (practicePlayerData.getPlayerState()) {
        case FIGHTING:
          Match match = plugin.getManagerHandler().getMatchManager().getMatch(practicePlayerData);
          if (match.getMatchState() != MatchState.FIGHTING) {
            e.setCancelled(true);
          }
          if (!match.getKit().isBridges() && e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            plugin.getManagerHandler().getMatchManager()
                .removeFighter(player, practicePlayerData, true);
          } else if (match.getKit().isBridges()
              && e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            e.setDamage(0.0D);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
              MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
              match.getKit().applyKit(player, practicePlayerData);
              player.teleport(playerTeam.getBridgeSpawnLocation());
              player.setHealth(player.getMaxHealth());
            }, 1L);
            player.updateInventory();
          }
          if ((match.getKit().isBridges() && e.getCause() == EntityDamageEvent.DamageCause.FALL)
              || (player.getFallDistance() >= 50
              && e.getCause() == EntityDamageEvent.DamageCause.FALL)
              || match.getKit().isNoFall() && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
          }
          break;
        case EVENT:
          PracticeEvent<?> event = plugin.getManagerHandler().getEventManager()
              .getEventPlaying(player);
          if (event != null) {
            if (event instanceof SumoEvent) {
              SumoEvent sumoEvent = (SumoEvent) event;
              SumoPlayer sumoPlayer = sumoEvent.getPlayer(player);
              if (sumoPlayer != null && sumoPlayer.getState() == SumoPlayer.SumoState.FIGHTING) {
                e.setCancelled(false);
              }
              if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                e.setCancelled(true);
              }
            } else if (event instanceof BracketsEvent) {
              BracketsEvent bracketsEvent = (BracketsEvent) event;
              BracketsPlayer bracketsPlayer = bracketsEvent.getPlayer(player);
              if (bracketsPlayer != null
                  && bracketsPlayer.getState() == BracketsPlayer.BracketsState.FIGHTING) {
                e.setCancelled(false);
              }
              if (e.getCause() == EntityDamageEvent.DamageCause.FALL
                  && bracketsPlayer.getState() == BracketsPlayer.BracketsState.WAITING) {
                e.setCancelled(true);
              }
            } else if (event instanceof OITCEvent) {
              OITCEvent oitcEvent = (OITCEvent) event;
              OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);
              e.setCancelled(
                  oitcPlayer == null || oitcPlayer.getState() != OITCPlayer.OITCState.FIGHTING);
            } else if (event instanceof LMSEvent) {
              LMSEvent lmsEvent = (LMSEvent) event;
              LMSPlayer lmsPlayer = lmsEvent.getPlayer(player);
              if (lmsPlayer != null && lmsPlayer.getState() == LMSPlayer.LMSState.WAITING
                  || lmsPlayer != null && lmsPlayer.getState() == LMSPlayer.LMSState.ELIMINATED) {
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                  e.setCancelled(true);
                }
              }
            } else if (event instanceof SkyWarsEvent) {
              SkyWarsEvent skyWarsEvent = (SkyWarsEvent) event;
              SkyWarsPlayer skyWarsPlayer = skyWarsEvent.getPlayer(player);
              if (skyWarsPlayer != null
                  && skyWarsPlayer.getState() == SkyWarsPlayer.SkyWarsState.WAITING
                  || skyWarsPlayer != null
                  && skyWarsPlayer.getState() == SkyWarsPlayer.SkyWarsState.ELIMINATED) {
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                  e.setCancelled(true);
                }
              }
            } else if (event instanceof TNTTagEvent || event instanceof ParkourEvent
                || event instanceof KnockoutEvent || event instanceof StopLightEvent
                || event instanceof SpleefEvent) {
              if (e.getCause() == EntityDamageEvent.DamageCause.FALL
                  || e.getCause() == EntityDamageEvent.DamageCause.FIRE
                  || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                e.setCancelled(true);
              }
            }
          }
          break;
        case SPECTATING:
          if (e.getCause() == EntityDamageEvent.DamageCause.FIRE
              || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            player.setFireTicks(0);
            e.setCancelled(true);
            return;
          }

          Location locationA;
          if (plugin.getManagerHandler().getEventManager().isSpectating(player)
              && plugin.getManagerHandler().getEventManager().getOngoingEvent() != null) {
            locationA = plugin.getManagerHandler().getEventManager().getOngoingEvent()
                .getFirstLocation();
          } else {
            Match spectatedMatch = plugin.getManagerHandler().getMatchManager()
                .getSpectatingMatch(player.getUniqueId());
            locationA =
                spectatedMatch.getStandaloneArena() != null ? spectatedMatch.getStandaloneArena()
                    .getA().toBukkitLocation()
                    : spectatedMatch.getArena().getA().toBukkitLocation();
          }
          if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            player.setHealth(player.getMaxHealth());
            if (locationA != null) {
              player.teleport(locationA);
            } else {
              player.teleport(plugin.getManagerHandler().getSpawnManager().getSpawnLocation()
                  .toBukkitLocation());
            }
          }
          e.setCancelled(true);
          break;
        default:
          if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            e.getEntity().teleport(
                plugin.getManagerHandler().getSpawnManager().getSpawnLocation().toBukkitLocation());
          }
          e.setCancelled(true);
          break;
      }
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      event.setCancelled(true);
      return;
    }
    if (event.getEntity().hasMetadata("NPC")) {
      event.setCancelled(true);
      return;
    }

    Player entity = (Player) event.getEntity();
    Player damager;

    if (event.getDamager() instanceof Player) {
      damager = (Player) event.getDamager();
    } else if (event.getDamager() instanceof Arrow && ((Projectile) event.getDamager()).getShooter()
        != ((Player) event.getEntity()).getPlayer() && event.getDamager() != null) {
      damager = (Player) ((Projectile) event.getDamager()).getShooter();
    } else {
      return;
    }

    PracticePlayerData entityData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(entity.getUniqueId());
    PracticePlayerData damagerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(damager.getUniqueId());

    boolean isEventEntity =
        plugin.getManagerHandler().getEventManager().getEventPlaying(entity) != null;
    boolean isEventDamager =
        plugin.getManagerHandler().getEventManager().getEventPlaying(damager) != null;

    PracticeEvent<?> eventDamager = plugin.getManagerHandler().getEventManager()
        .getEventPlaying(damager);
    PracticeEvent<?> eventEntity = plugin.getManagerHandler().getEventManager()
        .getEventPlaying(entity);

    if (!entity.canSee(damager) && damager.canSee(entity)) {
      event.setCancelled(true);
      return;
    }
    if (eventDamager != null) {
      if (eventDamager.getSpectators().contains(damager)) {
        event.setCancelled(true);
        return;
      }
    }

    if (isEventDamager && eventDamager instanceof SumoEvent
        && ((SumoEvent) eventDamager).getPlayer(damager).getState() != SumoPlayer.SumoState.FIGHTING
        || isEventEntity && eventDamager instanceof SumoEvent
        && ((SumoEvent) eventEntity).getPlayer(entity).getState() != SumoPlayer.SumoState.FIGHTING
        || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
        || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof BracketsEvent
        && ((BracketsEvent) eventDamager).getPlayer(damager).getState()
        != BracketsPlayer.BracketsState.FIGHTING
        || isEventEntity && eventDamager instanceof BracketsEvent
        && ((BracketsEvent) eventEntity).getPlayer(entity).getState()
        != BracketsPlayer.BracketsState.FIGHTING
        || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
        || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof LMSEvent
        && ((LMSEvent) eventDamager).getPlayer(damager).getState() != LMSPlayer.LMSState.FIGHTING
        || isEventEntity && eventDamager instanceof LMSEvent
        && ((LMSEvent) eventEntity).getPlayer(entity).getState() != LMSPlayer.LMSState.FIGHTING
        || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
        || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof KnockoutEvent
        && ((KnockoutEvent) eventDamager).getPlayer(damager).getState()
        != KnockoutPlayer.KnockoutState.FIGHTING
        || isEventEntity && eventDamager instanceof KnockoutEvent
        && ((KnockoutEvent) eventEntity).getPlayer(entity).getState()
        != KnockoutPlayer.KnockoutState.FIGHTING
        || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
        || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof SkyWarsEvent
        && ((SkyWarsEvent) eventDamager).getPlayer(damager).getState()
        != SkyWarsPlayer.SkyWarsState.FIGHTING
        || isEventEntity && eventDamager instanceof SkyWarsEvent
        && ((SkyWarsEvent) eventEntity).getPlayer(entity).getState()
        != SkyWarsPlayer.SkyWarsState.FIGHTING
        || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
        || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof ParkourEvent
        || isEventEntity && eventDamager instanceof ParkourEvent) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof FourCornersEvent
        || isEventEntity && eventDamager instanceof FourCornersEvent) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof ThimbleEvent
        || isEventEntity && eventDamager instanceof ThimbleEvent) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof DropperEvent
        || isEventEntity && eventDamager instanceof DropperEvent) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof StopLightEvent
        || isEventEntity && eventDamager instanceof StopLightEvent) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof SpleefEvent
        || isEventEntity && eventDamager instanceof SpleefEvent) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof OITCEvent
        || isEventEntity && eventEntity instanceof OITCEvent
        || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
        || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
      if (isEventEntity && isEventDamager && eventEntity instanceof OITCEvent
          && eventDamager instanceof OITCEvent) {
        OITCEvent oitcEvent = (OITCEvent) eventDamager;
        OITCPlayer oitcKiller = oitcEvent.getPlayer(damager);
        OITCPlayer oitcPlayer = oitcEvent.getPlayer(entity);
        if (oitcKiller.getState() != OITCPlayer.OITCState.FIGHTING
            || oitcPlayer.getState() != OITCPlayer.OITCState.FIGHTING) {
          event.setCancelled(true);
          return;
        }
        if (event.getDamager() instanceof Arrow) {
          Arrow arrow = (Arrow) event.getDamager();
          if (arrow.getShooter() instanceof Player) {
            if (damager != entity) {
              oitcPlayer.setLastKiller(oitcKiller);
              event.setDamage(0.0D);
              eventEntity.onDeath().accept(entity);
            }
          }
        }
      }
      return;
    }

    if (entityData == null || damagerData == null) {
      event.setCancelled(true);
      return;
    }

    if (entityData.getPlayerState() == PlayerState.FFA
        && damagerData.getPlayerState() != PlayerState.FFA) {
      event.setCancelled(true);
      return;
    }

    if (entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof SumoEvent
        || damagerData.getPlayerState() == PlayerState.EVENT && eventDamager instanceof SumoEvent) {
      event.setDamage(0.0D);
      return;
    }

    if (entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof BracketsEvent
        || damagerData.getPlayerState() == PlayerState.EVENT
        && eventDamager instanceof BracketsEvent) {
      return;
    }

    if (entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof LMSEvent
        || damagerData.getPlayerState() == PlayerState.EVENT && eventDamager instanceof LMSEvent) {
      return;
    }

    if (entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof KnockoutEvent
        || damagerData.getPlayerState() == PlayerState.EVENT
        && eventDamager instanceof KnockoutEvent) {
      event.setDamage(0.0D);
      return;
    }

    if (entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof SkyWarsEvent
        || damagerData.getPlayerState() == PlayerState.EVENT
        && eventDamager instanceof SkyWarsEvent) {
      return;
    }

    if (entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof TNTTagEvent
        || damagerData.getPlayerState() == PlayerState.EVENT
        && eventDamager instanceof TNTTagEvent) {
      TNTTagEvent tntTagEvent = (TNTTagEvent) eventEntity;
      event.setDamage(0.0D);
      if (tntTagEvent.getPlayer(entity).getState() == TNTTagPlayer.TNTTagState.ELIMINATED) {
        event.setCancelled(true);
      }
      if (tntTagEvent.getPlayer(damager).getState() == TNTTagPlayer.TNTTagState.TAGGED
          && tntTagEvent.getPlayer(entity).getState() == TNTTagPlayer.TNTTagState.INGAME) {
        tntTagEvent.tagPlayer(entity, damager);
        event.setDamage(0.0D);
      } else if (tntTagEvent.getPlayer(damager).getState() == TNTTagPlayer.TNTTagState.INGAME) {
        event.setDamage(0.0D);
      } else {
        event.setCancelled(true);
      }
      return;
    }

    if (entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof GulagEvent
        || damagerData.getPlayerState() == PlayerState.EVENT
        && eventDamager instanceof GulagEvent) {
      GulagEvent gulagEvent = (GulagEvent) eventEntity;
      if (gulagEvent.getPlayer(damager).getState() == GulagPlayer.GulagState.FIGHTING) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
          if (gulagEvent.getPlayer(entity).getState() == GulagPlayer.GulagState.WAITING
              || gulagEvent.getPlayer(entity).getState() == GulagPlayer.GulagState.PREPARING
              || gulagEvent.getPlayer(entity).getState() == GulagPlayer.GulagState.ELIMINATED) {
            event.setCancelled(true);
          }
        }
      }
      if (gulagEvent.getPlayer(damager).getState() == GulagPlayer.GulagState.FIGHTING) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
          event.setDamage(7.0D);
        } else {
          event.setDamage(1.5D);
        }
      } else {
        if (plugin.getSettingsConfig().getConfig()
            .getBoolean("SETTINGS.GENERAL.GULAG-PVP-WHILE-WAITING")) {
          event.setDamage(0.0D);
        } else {
          event.setCancelled(true);
        }
      }

      return;
    }

    Match match = plugin.getManagerHandler().getMatchManager().getMatch(entityData);
    if (match == null) {
      event.setDamage(0.0D);
      return;
    }
    if (match.getMatchState() != MatchState.FIGHTING) {
      return;
    }
    if (damagerData.getTeamId() == entityData.getTeamId() && !match.isFFA()) {
      event.setCancelled(true);
      return;
    }
    if (match.getKit().isSpleef() || match.getKit().isSumo() || match.getKit().isBoxing()
        || match.getKit().isBattleRush() || match.getKit().isMlgRush() || match.getKit()
        .isStickFight()) {
      event.setDamage(0.0D);
    }
    if (event.getDamager() instanceof Player) {
      damagerData.setCombo(damagerData.getCombo() + 1);
      damagerData.setHits(damagerData.getHits() + 1);
      if (damagerData.getCombo() > damagerData.getLongestCombo()) {
        damagerData.setLongestCombo(damagerData.getCombo());
      }
      entityData.setCombo(0);

      if (match.getKit().isSpleef()) {
        event.setCancelled(true);
      }
      if (match.getKit().isBoxing()) {
        if (damagerData.getHits() >= 100) {
          plugin.getManagerHandler().getMatchManager().removeFighter(entity, entityData, true);
        }
      }

      PracticePlayerData playerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(entity.getUniqueId());
      MatchTeam playerTeam = match.getTeams().get(playerData.getTeamId());

      double health = Math.ceil(entity.getHealth() - event.getFinalDamage()) / 2.0D;
      Player killer = (Player) event.getDamager();

      if (health <= 0.0d) {
        if (match.getKit().isBridges()) {
          if (killer != null) {
            if (SpecialMatchListener.playerKills.containsKey(killer.getUniqueId())) {
              SpecialMatchListener.playerKills.put(killer.getUniqueId(),
                  SpecialMatchListener.playerKills.get(killer.getUniqueId()) + 1);
            } else {
              SpecialMatchListener.playerKills.put(killer.getUniqueId(), 1);
            }
          }
          if (killer != null) {
            match.broadcast(
                (playerTeam.getTeamID() == 1 ? "&9" : "&c") + entity.getName() + "&7 was killed by "
                    + (playerTeam.getTeamID() == 1 ? "&c" : "&9") + killer.getName() + "&7.");
          }

          match.broadcastSound(Sound.ORB_PICKUP);

          event.setDamage(0.0D);
          event.setCancelled(true);
          SpecialEffects specialEffect = entityData.getPlayerSettings().getSpecialEffect();
          if (specialEffect != null && !specialEffect.getName().equals("None")) {
            plugin.getManagerHandler().getMatchManager()
                .playSpecialEffect(entity, match, specialEffect);
          }

          entity.teleport(playerTeam.getBridgeSpawnLocation());
          match.getKit().applyKit(entity, playerData);
          entity.resetMaxHealth();
          entity.setHealth(entity.getMaxHealth());
          entity.setFoodLevel(20);
          entity.removePotionEffect(PotionEffectType.ABSORPTION);

          plugin.getManagerHandler().getTimerManager().getTimer(BridgeArrowTimer.class)
              .clearCooldown(entity.getUniqueId());

          Board board = Board.getByPlayer(entity);
          if (board == null) {
            return;
          }

          BoardCooldown cooldown = board.getCooldown("arrow");
          if (cooldown != null) {
            cooldown.cancel();
            entity.setLevel(0);
            entity.setExp(0.0F);
          }
          return;
        }

        if (match.getKit().isBedWars()) {
          if (killer == null) {
            return;
          }

          SpecialEffects specialEffect = entityData.getPlayerSettings().getSpecialEffect();
          if (specialEffect != null && !specialEffect.getName().equals("None")) {
            plugin.getManagerHandler().getMatchManager()
                .playSpecialEffect(entity, match, specialEffect);
          }

          if (playerTeam.isHasBed() && !match.isEnding()) {
            match.broadcast(
                (playerTeam.getTeamID() == 1 ? "&9" : "&c") + entity.getName() + "&7 was killed by "
                    + (playerTeam.getTeamID() == 1 ? "&c" : "&9") + killer.getName() + "&7.");
            event.setDamage(0.0D);

            new BukkitRunnable() {
              int respawn = 4;

              @Override
              public void run() {
                if (respawn <= 1) {
                  if (!playerData.isInMatch()) {
                    cancel();
                    return;
                  }

                  entity.removePotionEffect(PotionEffectType.WEAKNESS);
                  entity.teleport(playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA()
                      .toBukkitLocation() : match.getStandaloneArena().getB().toBukkitLocation());

                  match.getTeams().forEach(
                      team -> team.alivePlayers().filter(player1 -> !entity.equals(player1))
                          .forEach(matchplayer -> matchplayer.sendMessage(CC.color(
                              (playerTeam.getTeamID() == 1 ? "&9" : "&c") + entity.getName()
                                  + "&a has respawned!"))));
                  match.getTeams().forEach(team -> team.alivePlayers().forEach(
                      matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, entity, false)));

                  entity.setFallDistance(50);
                  entity.setAllowFlight(false);
                  entity.setFlying(false);

                  entity.setHealth(entity.getMaxHealth());
                  entity.setFoodLevel(20);

                  entity.sendMessage(CC.color("&aYou have respawned!"));
                  entity.playSound(entity.getLocation(), Sound.ORB_PICKUP, 10, 1);

                  Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    match.getKit().applyKit(entity, entityData);
                    CC.sendTitle(entity, "&aRespawning...", "");
                    cancel();
                  }, 2L);
                }

                if (respawn == 4) {
                  entity.addPotionEffect(
                      new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0));

                  match.getTeams().forEach(team -> team.alivePlayers().forEach(
                      matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, entity, true)));

                  entity.getInventory().clear();
                  entity.getInventory().setArmorContents(null);
                  entity.updateInventory();

                  entity.setHealth(entity.getMaxHealth());
                  entity.setFoodLevel(20);

                  entity.setVelocity(entity.getVelocity().add(new Vector(0, 0.25, 0)));
                  entity.setAllowFlight(true);
                  entity.setFlying(true);
                  entity.setVelocity(entity.getVelocity().add(new Vector(0, 0.15, 0)));
                  entity.setAllowFlight(true);
                  entity.setFlying(true);

                  entity.teleport(killer.getLocation());
                }

                respawn = respawn - 1;
                CC.sendTitle(entity, "&a" + respawn, "");
                entity.playSound(entity.getLocation(), Sound.NOTE_PLING, 10, 1);
              }
            }.runTaskTimer(plugin, 0L, 20L);
          } else if (match.isPartyMatch()) {
            plugin.getManagerHandler().getMatchManager().removeFighter(entity, playerData, true);
          } else if (match.isEnding()) {
            entity.teleport(
                plugin.getManagerHandler().getSpawnManager().getSpawnLocation().toBukkitLocation());
          } else {
            match.setCanContinue(false);
            if (!match.isCanContinue() && !match.isEnding()) {
              MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
                  : ((playerData.getTeamId() == 0) ? match.getTeams().get(1)
                      : match.getTeams().get(0));
              Bukkit.getPluginManager()
                  .callEvent(new MatchEndEvent(match, opposingTeam, playerTeam));
              entity.teleport(
                  playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                      : match.getStandaloneArena().getB().toBukkitLocation());
              match.broadcastSound(Sound.FIREWORK_LAUNCH);
              match.broadcastSound(Sound.FIREWORK_TWINKLE2);
            }
          }
        }
      }
    } else if (event.getDamager() instanceof Arrow) {
      Arrow arrow = (Arrow) event.getDamager();
      if (arrow.getShooter() instanceof Player) {
        Player shooter = (Player) arrow.getShooter();
        if (!entity.getName().equals(shooter.getName())) {
          double health = Math.ceil(entity.getHealth() - event.getFinalDamage()) / 2.0D;
          if (health > 0.0D) {
            CC.sendMessage(shooter,
                plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.ARROW-HIT")
                    .replace("<player>", entity.getDisplayName())
                    .replace("<health>", String.valueOf(health))
            );
          }
        }
      }
    }
  }

  @EventHandler
  public void onPotionSplash(PotionSplashEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player)) {
      return;
    }

    for (PotionEffect effect : event.getEntity().getEffects()) {
      if (!effect.getType().equals(PotionEffectType.INVISIBILITY)) {
        Player shooter = (Player) event.getEntity().getShooter();
        if (shooter == null) {
          return;
        }

        PracticePlayerData shooterData = plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(shooter.getUniqueId());
        shooterData.setThrownPots(shooterData.getThrownPots() + 1);
        if (event.getIntensity(shooter) <= 0.5D) {
          shooterData.setMissedPots(shooterData.getMissedPots() + 1);
        }

        break;
      }
    }
  }
}
