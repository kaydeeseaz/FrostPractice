package dev.demeng.frost.managers;

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
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.cuboid.Cuboid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
public class EventManager {

  private final Frost plugin = Frost.getInstance();
  private final ConfigCursor config = new ConfigCursor(plugin.getMessagesConfig(),
      "MESSAGES.EVENT");

  private final Map<Class<? extends PracticeEvent<?>>, PracticeEvent<?>> events = new HashMap<>();
  private final HashMap<UUID, PracticeEvent<?>> spectators;
  @Setter private PracticeEvent<?> lastEvent;
  private final World eventWorld;

  public EventManager() {
    events.put(LMSEvent.class, new LMSEvent());
    events.put(OITCEvent.class, new OITCEvent());
    events.put(SumoEvent.class, new SumoEvent());
    events.put(GulagEvent.class, new GulagEvent());
    events.put(TNTTagEvent.class, new TNTTagEvent());
    events.put(SpleefEvent.class, new SpleefEvent());
    events.put(ParkourEvent.class, new ParkourEvent());
    events.put(ThimbleEvent.class, new ThimbleEvent());
    events.put(SkyWarsEvent.class, new SkyWarsEvent());
    events.put(DropperEvent.class, new DropperEvent());
    events.put(BracketsEvent.class, new BracketsEvent());
    events.put(KnockoutEvent.class, new KnockoutEvent());
    events.put(StopLightEvent.class, new StopLightEvent());
    events.put(FourCornersEvent.class, new FourCornersEvent());

    boolean newWorld;

    if (plugin.getServer().getWorld("event") == null) {
      eventWorld = plugin.getServer()
          .createWorld(new WorldCreator("event").type(WorldType.FLAT).generatorSettings("2;0;1;"));
      newWorld = true;
    } else {
      eventWorld = plugin.getServer().getWorld("event");
      newWorld = false;
    }

    this.spectators = new HashMap<>();

    if (eventWorld != null) {
      if (newWorld) {
        plugin.getServer().getWorlds().add(eventWorld);
      }
      eventWorld.setTime(2000L);
      eventWorld.setGameRuleValue("doDaylightCycle", "false");
      eventWorld.setGameRuleValue("doMobSpawning", "false");
      eventWorld.setStorm(false);
      eventWorld.getEntities().stream().filter(entity -> !(entity instanceof Player))
          .forEach(Entity::remove);
    }

    if (plugin.getManagerHandler().getSpawnManager().getSkywarsMin() != null
        && plugin.getManagerHandler().getSpawnManager().getSkywarsMax() != null) {
      Cuboid cuboid = new Cuboid(
          getPlugin().getManagerHandler().getSpawnManager().getSkywarsMin().toBukkitLocation(),
          getPlugin().getManagerHandler().getSpawnManager().getSkywarsMax().toBukkitLocation());
      SkyWarsEvent skyWarsEvent = (SkyWarsEvent) getByName("SkyWars");
      skyWarsEvent.setCuboid(cuboid);
      if (skyWarsEvent.getCuboid() != null) {
        ChunkRestorationManager.getIChunkRestoration().copy(skyWarsEvent.getCuboid());
      }
    }
  }

  public PracticeEvent<?> getOngoingEvent() {
    return this.events.values().stream().filter(event -> event.getState() != EventState.UNANNOUNCED)
        .findFirst().orElse(null);
  }

  public PracticeEvent<?> getByName(String name) {
    return events.values().stream().filter(event -> event.getName().equalsIgnoreCase(name))
        .findFirst().orElse(null);
  }

  public void hostEvent(PracticeEvent<?> event, Player host) {
    event.setState(EventState.WAITING);
    event.setHost(host);
    event.startCountdown();
  }

  public void hostEvent(PracticeEvent<?> event, Kit kit, int limit, Player host) {
    event.setState(EventState.WAITING);
    event.setKitOptional(Optional.of(kit));
    event.setLimit(limit);
    event.setHost(host);
    event.startCountdown();
  }

  public void addSpectator(Player player, PracticePlayerData practicePlayerData,
      PracticeEvent<?> event) {
    if (event.getState() == EventState.UNANNOUNCED) {
      return;
    }

    this.spectators.put(player.getUniqueId(), event);
    event.sendMessage(config.getString("SPECTATOR-JOIN").replace("<player>", player.getName())
        .replace("<eventName>", event.getName()));

    practicePlayerData.setPlayerState(PlayerState.SPECTATING);
    if (event instanceof TNTTagEvent) {
      player.teleport(getPlugin().getManagerHandler().getSpawnManager().getTntTagGameLocation()
          .toBukkitLocation());
    } else if (event instanceof KnockoutEvent) {
      player.teleport(getPlugin().getManagerHandler().getSpawnManager().getKnockoutLocation()
          .toBukkitLocation());
    } else {
      player.teleport(event.getFirstLocation());
    }

    player.setAllowFlight(true);
    player.setFlying(true);

    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    player.setHealth(player.getMaxHealth());
    player.setSaturation(20.0F);

    plugin.getManagerHandler().getItemManager().getSpecItems()
        .stream()
        .filter(ItemManager.HotbarItem::isEnabled)
        .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack())
        );

    player.updateInventory();
    for (Player online : plugin.getServer().getOnlinePlayers()) {
      PlayerUtil.hideOrShowPlayer(online, player, true);
    }
  }

  public void removeSpectator(Player player, PracticeEvent<?> event) {
    event.sendMessage(config.getString("SPECTATOR-LEAVE").replace("<player>", player.getName())
        .replace("<eventName>", event.getName()));

    this.spectators.remove(player.getUniqueId(), event);
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);
  }

  public boolean isSpectating(Player player) {
    return this.spectators.containsKey(player.getUniqueId());
  }

  public boolean isPlaying(Player player, PracticeEvent<?> event) {
    return event.getPlayers().containsKey(player.getUniqueId());
  }

  public PracticeEvent<?> getEventPlaying(Player player) {
    return this.events.values().stream().filter(event -> this.isPlaying(player, event)).findFirst()
        .orElse(null);
  }
}