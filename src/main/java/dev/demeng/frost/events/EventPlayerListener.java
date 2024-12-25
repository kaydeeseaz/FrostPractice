package dev.demeng.frost.events;

import dev.demeng.frost.Frost;
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
import dev.demeng.frost.user.player.PracticePlayerData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EventPlayerListener implements Listener {

  private final Frost plugin;

  // TODO: Debug and fix this class, it's not working properly right now.
  // TODO: The players can't receive, nor deal damage to other players if this listener is loaded.

  public EventPlayerListener(Frost plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player) {
      Player player = (Player) event.getEntity();
      if (player == null) {
        return;
      }

      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (practicePlayerData.isInEvent()) {
        PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
            .getEventPlaying(player);
        if (practiceEvent != null) {
          switch (practiceEvent.getName().toLowerCase()) {
            case "oitc":
              OITCEvent oitcEvent = (OITCEvent) practiceEvent;
              OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);
              event.setCancelled(
                  oitcPlayer == null || oitcPlayer.getState() != OITCPlayer.OITCState.FIGHTING);
              break;
            case "sumo":
              SumoEvent sumoEvent = (SumoEvent) practiceEvent;
              SumoPlayer sumoPlayer = sumoEvent.getPlayer(player);
              if (sumoPlayer != null && sumoPlayer.getState() == SumoPlayer.SumoState.FIGHTING) {
                event.setCancelled(false);
              }
              if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
              }
              break;
            case "brackets":
              BracketsEvent bracketsEvent = (BracketsEvent) practiceEvent;
              BracketsPlayer bracketsPlayer = bracketsEvent.getPlayer(player);
              if (bracketsPlayer != null
                  && bracketsPlayer.getState() == BracketsPlayer.BracketsState.FIGHTING) {
                event.setCancelled(false);
              }
              if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                  && bracketsPlayer.getState() == BracketsPlayer.BracketsState.WAITING) {
                event.setCancelled(true);
              }
              break;
            case "lms":
              LMSEvent lmsEvent = (LMSEvent) practiceEvent;
              LMSPlayer lmsPlayer = lmsEvent.getPlayer(player);
              if (lmsPlayer != null && lmsPlayer.getState() == LMSPlayer.LMSState.WAITING
                  || lmsPlayer != null && lmsPlayer.getState() == LMSPlayer.LMSState.ELIMINATED) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                  event.setCancelled(true);
                }
              }
              break;
            case "skywars":
              SkyWarsEvent skyWarsEvent = (SkyWarsEvent) practiceEvent;
              SkyWarsPlayer skyWarsPlayer = skyWarsEvent.getPlayer(player);
              if (skyWarsPlayer != null
                  && skyWarsPlayer.getState() == SkyWarsPlayer.SkyWarsState.WAITING
                  || skyWarsPlayer != null
                  && skyWarsPlayer.getState() == SkyWarsPlayer.SkyWarsState.ELIMINATED) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                  event.setCancelled(true);
                }
              }
              break;
            case "parkour":
            case "spleef":
            case "stoplight":
            case "knockout":
            case "tnttag":
              if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                  || event.getCause() == EntityDamageEvent.DamageCause.FIRE
                  || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
              }
              break;
          }
        }
      }
      if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
        event.getEntity().teleport(
            plugin.getManagerHandler().getSpawnManager().getSpawnLocation().toBukkitLocation());
      }

      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) {
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
    if (!damagerData.isInEvent() || !entityData.isInEvent()) {
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
        && ((SumoEvent) eventEntity).getPlayer(entity).getState()
        != SumoPlayer.SumoState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof BracketsEvent
        && ((BracketsEvent) eventDamager).getPlayer(damager).getState()
        != BracketsPlayer.BracketsState.FIGHTING
        || isEventEntity && eventDamager instanceof BracketsEvent
        && ((BracketsEvent) eventEntity).getPlayer(entity).getState()
        != BracketsPlayer.BracketsState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof LMSEvent
        && ((LMSEvent) eventDamager).getPlayer(damager).getState() != LMSPlayer.LMSState.FIGHTING
        || isEventEntity && eventDamager instanceof LMSEvent
        && ((LMSEvent) eventEntity).getPlayer(entity).getState() != LMSPlayer.LMSState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof KnockoutEvent
        && ((KnockoutEvent) eventDamager).getPlayer(damager).getState()
        != KnockoutPlayer.KnockoutState.FIGHTING
        || isEventEntity && eventDamager instanceof KnockoutEvent
        && ((KnockoutEvent) eventEntity).getPlayer(entity).getState()
        != KnockoutPlayer.KnockoutState.FIGHTING) {
      event.setCancelled(true);
      return;
    }

    if (isEventDamager && eventDamager instanceof SkyWarsEvent
        && ((SkyWarsEvent) eventDamager).getPlayer(damager).getState()
        != SkyWarsPlayer.SkyWarsState.FIGHTING
        || isEventEntity && eventDamager instanceof SkyWarsEvent
        && ((SkyWarsEvent) eventEntity).getPlayer(entity).getState()
        != SkyWarsPlayer.SkyWarsState.FIGHTING) {
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
        || isEventEntity && eventEntity instanceof OITCEvent) {
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

    if (entityData.isInEvent() && eventEntity instanceof SumoEvent
        || damagerData.isInEvent() && eventDamager instanceof SumoEvent) {
      event.setDamage(0.0D);
      return;
    }

    if (entityData.isInEvent() && eventEntity instanceof BracketsEvent
        || damagerData.isInEvent() && eventDamager instanceof BracketsEvent) {
      return;
    }

    if (entityData.isInEvent() && eventEntity instanceof LMSEvent
        || damagerData.isInEvent() && eventDamager instanceof LMSEvent) {
      return;
    }

    if (entityData.isInEvent() && eventEntity instanceof KnockoutEvent
        || damagerData.isInEvent() && eventDamager instanceof KnockoutEvent) {
      event.setDamage(0.0D);
      return;
    }

    if (entityData.isInEvent() && eventEntity instanceof SkyWarsEvent
        || damagerData.isInEvent() && eventDamager instanceof SkyWarsEvent) {
      return;
    }

    if (entityData.isInEvent() && eventEntity instanceof TNTTagEvent
        || damagerData.isInEvent() && eventDamager instanceof TNTTagEvent) {
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

    if (entityData.isInEvent() && eventEntity instanceof GulagEvent
        || damagerData.isInEvent() && eventDamager instanceof GulagEvent) {
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
    }
  }
}
