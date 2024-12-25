package dev.demeng.frost.events;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.games.brackets.BracketsEvent;
import dev.demeng.frost.events.games.brackets.BracketsPlayer;
import dev.demeng.frost.events.games.corners.FourCornersEvent;
import dev.demeng.frost.events.games.dropper.DropperEvent;
import dev.demeng.frost.events.games.gulag.GulagEvent;
import dev.demeng.frost.events.games.gulag.GulagPlayer;
import dev.demeng.frost.events.games.knockout.KnockoutEvent;
import dev.demeng.frost.events.games.lms.LMSEvent;
import dev.demeng.frost.events.games.oitc.OITCEvent;
import dev.demeng.frost.events.games.oitc.OITCPlayer;
import dev.demeng.frost.events.games.parkour.ParkourEvent;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.events.games.spleef.SpleefEvent;
import dev.demeng.frost.events.games.stoplight.StopLightEvent;
import dev.demeng.frost.events.games.sumo.SumoEvent;
import dev.demeng.frost.events.games.sumo.SumoPlayer;
import dev.demeng.frost.events.games.thimble.ThimbleEvent;
import dev.demeng.frost.events.games.tnttag.TNTTagEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.game.event.EventStartEvent;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.managers.PlayerManager;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class PracticeEvent<K extends EventPlayer> {

  private final Frost plugin = Frost.getInstance();

  private final String name;
  private final ConfigCursor pluginSettings = new ConfigCursor(plugin.getSettingsConfig(),
      "SETTINGS");

  private Player host;
  private int limit = 30;
  private Optional<Kit> kitOptional;

  private List<UUID> playersX = new ArrayList<>();
  private EventState state = EventState.UNANNOUNCED;

  public void startCountdown() {
    if (getCountdownTask().isEnded()) {
      getCountdownTask().setTimeUntilStart(getCountdownTask().getCountdownTime());
      getCountdownTask().setEnded(false);
    } else {
      getCountdownTask().runTaskTimer(plugin, 20L, 20L);
    }
  }

  public void sendMessage(String message) {
    for (Player player : getBukkitPlayers()) {
      CC.sendMessage(player, message);
    }
  }

  public Set<Player> getSpectators() {
    return plugin.getManagerHandler().getEventManager().getSpectators().keySet().stream()
        .filter(uuid -> plugin.getServer().getPlayer(uuid) != null)
        .map(plugin.getServer()::getPlayer).collect(Collectors.toSet());
  }

  public Set<Player> getBukkitPlayers() {
    return getPlayers().keySet().stream()
        .filter(uuid -> plugin.getServer().getPlayer(uuid) != null)
        .map(plugin.getServer()::getPlayer)
        .collect(Collectors.toSet());
  }

  public void join(Player player) {
    if (getPlayers().size() >= limit) {
      return;
    }

    playersX.add(player.getUniqueId());

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    practicePlayerData.setPlayerState(PlayerState.EVENT);
    PlayerUtil.clearPlayer(player, false);
    if (onJoin() != null) {
      onJoin().accept(player);
    }
    if (getSpawnLocations().size() == 1) {
      player.teleport(getSpawnLocations().get(0).toBukkitLocation());
    } else if (this instanceof OITCEvent) {
      player.teleport(
          getSpawnLocations().get(ThreadLocalRandom.current().nextInt(getSpawnLocations().size()))
              .toBukkitLocation());
    } else if (this instanceof LMSEvent) {
      player.teleport(
          plugin.getManagerHandler().getSpawnManager().getLmsLocation().toBukkitLocation());
    } else if (this instanceof KnockoutEvent) {
      player.teleport(
          plugin.getManagerHandler().getSpawnManager().getKnockoutLocation().toBukkitLocation());
    } else if (this instanceof SkyWarsEvent) {
      player.teleport(
          plugin.getManagerHandler().getSpawnManager().getSkywarsLocation().toBukkitLocation());
    } else if (this instanceof SpleefEvent) {
      player.teleport(
          plugin.getManagerHandler().getSpawnManager().getSpleefLocation().toBukkitLocation());
    }

    plugin.getManagerHandler().getPlayerManager().giveLobbyItems(player);
    getBukkitPlayers().forEach(other -> PlayerUtil.hideOrShowPlayer(other, player, false));
    getBukkitPlayers().forEach(other -> PlayerUtil.hideOrShowPlayer(player, other, false));

    sendMessage(plugin.getMessagesConfig().getConfig().getString("MESSAGES.EVENT.PLAYER-JOINED")
        .replace("<eventName>", name)
        .replace("<player>", player.getName())
        .replace("<players>", String.valueOf(getPlayers().size()))
        .replace("<maxPlayers>", String.valueOf(limit))
    );
    CC.sendMessage(player, plugin.getMessagesConfig().getConfig().getString("MESSAGES.EVENT.JOINED")
        .replace("<eventName>", name));
  }

  public void leave(Player player) {
    if (this instanceof OITCEvent) {
      OITCEvent oitcEvent = (OITCEvent) this;
      OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);
      oitcPlayer.setState(OITCPlayer.OITCState.ELIMINATED);
    }

    playersX.remove(player.getUniqueId());
    if (onDeath() != null) {
      onDeath().accept(player);
    }

    getPlayers().remove(player.getUniqueId());

    sendMessage(plugin.getMessagesConfig().getConfig().getString("MESSAGES.EVENT.PLAYER-LEFT")
        .replace("<eventName>", name)
        .replace("<player>", player.getName())
        .replace("<players>", String.valueOf(getPlayers().size()))
        .replace("<maxPlayers>", String.valueOf(limit))
    );
    CC.sendMessage(player, plugin.getMessagesConfig().getConfig().getString("MESSAGES.EVENT.LEFT")
        .replace("<eventName>", name));

    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);
  }

  public void start() {
    new EventStartEvent(this).call();

    if (plugin.isUsingCustomKB()) {
      playersX.forEach(p -> PlayerManager.setKnockbackProfile(Bukkit.getPlayer(p), this.name));
    }

    setState(EventState.STARTED);
    onStart();
  }

  public void handleWin(Player winner) {
    for (String message : plugin.getMessagesConfig().getConfig()
        .getStringList("MESSAGES.EVENT.ENDED")) {
      Bukkit.getServer().broadcastMessage(
          CC.color(message.replace("<winner>", winner.getName()).replace("<eventName>", name)));
    }
  }

  public void end() {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      for (Player player : plugin.getManagerHandler().getEventManager().getEventWorld()
          .getPlayers()) {
        plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);
      }
    }, 2L);
    plugin.getManagerHandler().getEventManager().setLastEvent(this);

    playersX.clear();

    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getLastEvent();
    switch (event.getName().toLowerCase()) {
      case "lms":
        LMSEvent lmsEvent = (LMSEvent) this;
        lmsEvent.cancelAll();
        Bukkit.getWorld("event").getEntities().stream().filter(entity -> entity instanceof Item)
            .forEach(Entity::remove);
        break;
      case "knockout":
        KnockoutEvent knockoutEvent = (KnockoutEvent) this;
        knockoutEvent.cancelAll();
        Bukkit.getWorld("event").getEntities().stream().filter(entity -> entity instanceof Item)
            .forEach(Entity::remove);
        break;
      case "tnttag":
        TNTTagEvent tntTagEvent = (TNTTagEvent) this;
        if (tntTagEvent.getTntTagTask() != null) {
          tntTagEvent.getTntTagTask().cancel();
          tntTagEvent.setTntTagTask(null);
        }
        break;
      case "oitc":
        OITCEvent oitcEvent = (OITCEvent) this;
        if (oitcEvent.getGameTask() != null) {
          oitcEvent.getGameTask().cancel();
        }
        oitcEvent.setRunning(false);
        break;
      case "sumo":
        SumoEvent sumoEvent = (SumoEvent) this;
        for (SumoPlayer sumoPlayer : sumoEvent.getPlayers().values()) {
          if (sumoPlayer.getFightTask() != null) {
            sumoPlayer.getFightTask().cancel();
          }
        }
        if (sumoEvent.getWaterCheckTask() != null) {
          sumoEvent.getWaterCheckTask().cancel();
        }
        break;
      case "gulag":
        GulagEvent gulagEvent = (GulagEvent) this;
        for (GulagPlayer gulagPlayer : gulagEvent.getPlayers().values()) {
          if (gulagPlayer.getFightTask() != null) {
            gulagPlayer.getFightTask().cancel();
          }
        }
        break;
      case "parkour":
        ParkourEvent parkourEvent = (ParkourEvent) this;
        if (parkourEvent.getGameTask() != null) {
          parkourEvent.getGameTask().cancel();
        }
        if (parkourEvent.getWaterCheckTask() != null) {
          parkourEvent.getWaterCheckTask().cancel();
        }
        break;
      case "skywars":
        SkyWarsEvent skyWarsEvent = (SkyWarsEvent) this;
        skyWarsEvent.cancelAll();
        skyWarsEvent.resetSpawns();
        for (Entity entity : Bukkit.getWorld("event").getEntities()) {
          if (entity instanceof Item) {
            entity.remove();
          }
        }
        Bukkit.getScheduler().runTask(plugin,
            () -> ChunkRestorationManager.getIChunkRestoration().reset(skyWarsEvent.getCuboid()));
        break;
      case "4corners":
        FourCornersEvent fourCornersEvent = (FourCornersEvent) this;
        if (fourCornersEvent.getGameTask() != null) {
          fourCornersEvent.getGameTask().cancel();
        }
        break;
      case "thimble":
        ThimbleEvent thimbleEvent = (ThimbleEvent) this;
        thimbleEvent.cancelAll();
        Bukkit.getScheduler().runTask(plugin, () -> {
          for (Block block : thimbleEvent.getBlocks().values()) {
            block.setType(Material.WATER);
            thimbleEvent.getBlocks().remove(block.getLocation());
          }
          thimbleEvent.getBlocks().clear();
        });
        break;
      case "dropper":
        DropperEvent dropperEvent = (DropperEvent) this;
        dropperEvent.cancelAll();
        break;
      case "spleef":
        SpleefEvent spleefEvent = (SpleefEvent) this;
        Bukkit.getScheduler().runTask(plugin, () -> {
          for (Location block : spleefEvent.getBrokenBlocks()) {
            block.getBlock().setType(Material.SNOW_BLOCK);
            spleefEvent.getBrokenBlocks().remove(block);
          }
          spleefEvent.getBrokenBlocks().clear();
        });
        spleefEvent.cancelAll();
        break;
      case "stoplight":
        StopLightEvent stopLightEvent = (StopLightEvent) this;
        if (stopLightEvent.getGameTask() != null) {
          stopLightEvent.getGameTask().cancel();
        }
        break;
      case "brackets":
        BracketsEvent bracketsEvent = (BracketsEvent) this;
        for (BracketsPlayer bracketsPlayer : bracketsEvent.getPlayers().values()) {
          if (bracketsPlayer.getFightTask() != null) {
            bracketsPlayer.getFightTask().cancel();
          }
        }
        break;
    }

    getPlayers().clear();
    setState(EventState.UNANNOUNCED);

    Iterator<UUID> iterator = plugin.getManagerHandler().getEventManager().getSpectators().keySet()
        .iterator();
    while (iterator.hasNext()) {
      UUID spectatorUUID = iterator.next();
      Player spectator = Bukkit.getPlayer(spectatorUUID);
      if (spectator != null) {
        plugin.getServer().getScheduler().runTask(plugin,
            () -> plugin.getManagerHandler().getPlayerManager()
                .resetPlayerOrSpawn(spectator, true));
        iterator.remove();
      }
    }

    plugin.getManagerHandler().getEventManager().getSpectators().clear();
    getCountdownTask().setEnded(true);
  }

  public Location getFirstLocation() {
    return this.getSpawnLocations().get(0).toBukkitLocation();
  }

  public K getPlayer(Player player) {
    return getPlayer(player.getUniqueId());
  }

  public K getPlayer(UUID uuid) {
    return getPlayers().get(uuid);
  }

  public abstract Map<UUID, K> getPlayers();

  public abstract EventCountdownTask getCountdownTask();

  public abstract List<CustomLocation> getSpawnLocations();

  public abstract void onStart();

  public abstract Consumer<Player> onJoin();

  public abstract Consumer<Player> onDeath();
}
