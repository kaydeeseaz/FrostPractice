package dev.demeng.frost.game.match;

import static dev.demeng.frost.util.CC.sendMessage;
import static dev.demeng.frost.util.CC.sendTitle;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.user.ui.postmatch.InventorySnapshot;
import dev.demeng.frost.util.TimeUtils;
import io.netty.util.internal.ConcurrentSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Match {

  private final Frost plugin = Frost.getInstance();

  private final Map<UUID, InventorySnapshot> snapshots = new HashMap<>();

  private final Set<Entity> entitiesToRemove = new HashSet<>();
  private final Set<Location> placedBlocksLocations = new ConcurrentSet<>();
  private final Set<UUID> spectators = new ConcurrentSet<>();
  private final Set<Integer> runnables = new HashSet<>();

  private final List<MatchTeam> teams;

  private final UUID matchId = UUID.randomUUID();
  private final QueueType type;
  private final Arena arena;
  private final Kit kit;

  private StandaloneArena standaloneArena;
  private MatchState matchState = MatchState.STARTING;
  private int winningTeamId;
  private int countdown = 6;
  private boolean canMove = true;
  private boolean canContinue = true;
  private int durationTimer;

  public Match(Arena arena, Kit kit, QueueType type, MatchTeam... teams) {
    this.arena = arena;
    this.kit = kit;
    this.type = type;
    this.teams = Arrays.asList(teams);
  }

  public MatchTeam getTeamById(int id) {
    return this.getTeams().stream().filter(matchTeam -> matchTeam.getTeamID() == id).findFirst()
        .orElse(null);
  }

  public String getDuration() {
    return TimeUtils.formatIntoMMSS(durationTimer);
  }

  public boolean isStarting() {
    return this.matchState == MatchState.STARTING;
  }

  public boolean isFighting() {
    return this.matchState == MatchState.FIGHTING;
  }

  public boolean isEnding() {
    return this.matchState == MatchState.ENDING;
  }

  public boolean isBusy() {
    return this.isFighting() || this.isEnding() || this.isStarting();
  }

  public void addSpectator(UUID uuid) {
    this.spectators.add(uuid);
  }

  public void removeSpectator(UUID uuid) {
    this.spectators.remove(uuid);
  }

  public void addSnapshot(Player player) {
    this.snapshots.put(player.getUniqueId(), new InventorySnapshot(player, this));
  }

  public boolean hasSnapshot(UUID uuid) {
    return this.snapshots.containsKey(uuid);
  }

  public InventorySnapshot getSnapshot(UUID uuid) {
    return this.snapshots.get(uuid);
  }

  public void addEntityToRemove(Entity entity) {
    this.entitiesToRemove.add(entity);
  }

  public void removeEntityToRemove(Entity entity) {
    this.entitiesToRemove.remove(entity);
  }

  public void clearEntitiesToRemove() {
    this.entitiesToRemove.clear();
  }

  public void addRunnable(int id) {
    this.runnables.add(id);
  }

  public void addPlacedBlock(Block block) {
    this.placedBlocksLocations.add(block.getLocation());
  }

  public void removePlacedBlock(Block block) {
    this.placedBlocksLocations.remove(block.getLocation());
  }

  public void broadcastWithSound(String message, Sound sound) {
    this.teams.forEach(team -> team.alivePlayers().forEach(player -> {
      sendMessage(player, message);
      player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }));
    this.spectatorPlayers().forEach(spectator -> {
      sendMessage(spectator, message);
      spectator.playSound(spectator.getLocation(), sound, 1.0f, 1.0f);
    });
  }

  public void broadcastTitle(String message, String subMessage) {
    this.teams.forEach(
        team -> team.alivePlayers().forEach(player -> sendTitle(player, message, subMessage)));
    this.spectatorPlayers().forEach(spectator -> sendTitle(spectator, message, subMessage));
  }

  public void broadcastSound(Sound sound) {
    this.teams.forEach(team -> team.alivePlayers()
        .forEach(player -> player.playSound(player.getLocation(), sound, 10, 1)));
    this.spectatorPlayers()
        .forEach(spectator -> spectator.playSound(spectator.getLocation(), sound, 10, 1));
  }

  public void broadcast(String message) {
    this.teams.forEach(team -> team.alivePlayers().forEach(player -> sendMessage(player, message)));
    this.spectatorPlayers().forEach(spectator -> sendMessage(spectator, message));
  }

  public void broadcast(BaseComponent message) {
    this.teams.forEach(
        team -> team.alivePlayers().forEach(player -> player.spigot().sendMessage(message)));
    this.spectatorPlayers().forEach(spectator -> spectator.spigot().sendMessage(message));
  }

  public Stream<Player> spectatorPlayers() {
    return this.spectators.stream().map(plugin.getServer()::getPlayer).filter(Objects::nonNull);
  }

  public int decrementCountdown() {
    return --this.countdown;
  }

  public void incrementDuration() {
    ++this.durationTimer;
  }

  public boolean isFFA() {
    return this.teams.size() == 1;
  }

  public boolean isParty() {
    return this.isFFA()
        || this.teams.get(0).getPlayers().size() != 1 && this.teams.get(1).getPlayers().size() != 1;
  }

  public boolean isPartyMatch() {
    return this.isFFA() || (this.teams.get(0).getPlayers().size() >= 2
        || this.teams.get(1).getPlayers().size() >= 2);
  }

  public boolean isBreakable(Block block) {
    if (placedBlocksLocations.contains(block.getLocation())) {
      return true;
    }

    Material material = block.getType();
    switch (material) {
      case STAINED_CLAY:
        byte data = block.getData();
        if (data == 0 || data == 11 || data == 14) {
          return kit.isBridges();
        }
        return false;
      case SNOW_BLOCK:
      case SNOW:
        return kit.isSpleef();
      case WOOD:
      case BED_BLOCK:
      case ENDER_STONE:
        return kit.isBedWars() || kit.isMlgRush();
    }

    return false;
  }

  public boolean isPlaceable(Player player, Match match) {
    double minX = match.getStandaloneArena().getMin().getX();
    double minZ = match.getStandaloneArena().getMin().getZ();
    double maxX = match.getStandaloneArena().getMax().getX();
    double maxZ = match.getStandaloneArena().getMax().getZ();

    if (minX > maxX) {
      double lastMinX = minX;
      minX = maxX;
      maxX = lastMinX;
    }
    if (minZ > maxZ) {
      double lastMinZ = minZ;
      minZ = maxZ;
      maxZ = lastMinZ;
    }

    return !(player.getLocation().getX() >= minX) || !(player.getLocation().getX() <= maxX) || !(
        player.getLocation().getZ() >= minZ) || !(player.getLocation().getZ() <= maxZ);
  }
}
