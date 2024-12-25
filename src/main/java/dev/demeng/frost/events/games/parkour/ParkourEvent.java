package dev.demeng.frost.events.games.parkour;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.managers.ItemManager;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.PlayerUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParkourEvent extends PracticeEvent<ParkourPlayer> {

  private final Map<UUID, ParkourPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  @Getter private ParkourGameTask gameTask;
  @Getter private WaterCheckTask waterCheckTask;
  private List<UUID> visibility;

  public ParkourEvent() {
    super("Parkour");
  }

  @Override
  public Map<UUID, ParkourPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        this.getPlugin().getManagerHandler().getSpawnManager().getParkourLocation());
  }

  @Override
  public void onStart() {
    this.gameTask = new ParkourGameTask();
    this.gameTask.runTaskTimerAsynchronously(getPlugin(), 0, 20L);
    this.waterCheckTask = new WaterCheckTask();
    this.waterCheckTask.runTaskTimer(getPlugin(), 0, 0L);
    this.visibility = new ArrayList<>();
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(),
        new ParkourPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> sendMessage(
        CC.color("&8[&b&lEvent&8] &c" + player.getDisplayName() + " has left the event."));
  }

  public void toggleVisibility(Player player) {
    if (visibility.contains(player.getUniqueId())) {
      for (Player online : getBukkitPlayers()) {
        PlayerUtil.hideOrShowPlayer(player, online, false);
      }

      visibility.remove(player.getUniqueId());
      CC.sendMessage(player,
          getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.PARKOUR-SHOW"));
      return;
    }

    for (Player online : getBukkitPlayers()) {
      PlayerUtil.hideOrShowPlayer(player, online, true);
    }

    visibility.add(player.getUniqueId());
    CC.sendMessage(player,
        getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.PARKOUR-HIDE"));
  }

  private void teleportToSpawnOrCheckpoint(Player player) {
    new BukkitRunnable() {
      @Override
      public void run() {
        ParkourPlayer parkourPlayer = getPlayer(player.getUniqueId());
        if (parkourPlayer != null && parkourPlayer.getLastCheckpoint() != null) {
          player.teleport(parkourPlayer.getLastCheckpoint().toBukkitLocation());
          CC.sendMessage(player, getPlugin().getMessagesConfig().getConfig()
              .getString("MESSAGES.EVENT.PARKOUR-LAST-CHECKPOINT"));
          return;
        }

        CC.sendMessage(player, getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.PARKOUR-LAST-CHECKPOINT"));
        player.teleport(getPlugin().getManagerHandler().getSpawnManager().getParkourGameLocation()
            .toBukkitLocation());
      }
    }.runTask(getPlugin());
  }

  private void giveItems(Player player) {
    this.getPlugin().getServer().getScheduler().runTask(this.getPlugin(), () -> {
      PlayerUtil.clearPlayer(player, false);
      getPlugin().getManagerHandler().getItemManager().getParkourItems().stream()
          .filter(ItemManager.HotbarItem::isEnabled)
          .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack()));
      player.updateInventory();
    });
  }

  private Player getRandomPlayer() {
    if (getByState(ParkourPlayer.ParkourState.INGAME).size() == 0) {
      return null;
    }

    List<UUID> fighting = getByState(ParkourPlayer.ParkourState.INGAME);
    Collections.shuffle(fighting);
    UUID uuid = fighting.get(ThreadLocalRandom.current().nextInt(fighting.size()));

    return getPlugin().getServer().getPlayer(uuid);
  }

  public List<UUID> getByState(ParkourPlayer.ParkourState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(ParkourPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class ParkourGameTask extends BukkitRunnable {

    private int time = 303;

    @Override
    public void run() {
      if (time == 303) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.STARTING-IN")
                .replace("<countdown>", String.valueOf(time - 300)));
      } else if (time == 302) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.STARTING-IN")
                .replace("<countdown>", String.valueOf(time - 300)));
      } else if (time == 301) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.STARTING-IN")
                .replace("<countdown>", String.valueOf(time - 300)));
      } else if (time == 300) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.STARTED"));

        for (ParkourPlayer player : getPlayers().values()) {
          player.setLastCheckpoint(null);
          player.setState(ParkourPlayer.ParkourState.INGAME);
          player.setCheckpointId(0);
        }
        for (Player player : getBukkitPlayers()) {
          teleportToSpawnOrCheckpoint(player);
          giveItems(player);
        }
      } else if (time <= 0) {
        Player winner = getRandomPlayer();
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancel();
        return;
      }

      if (getPlayers().size() == 1) {
        Player winner = Bukkit.getPlayer(getByState(ParkourPlayer.ParkourState.INGAME).get(0));
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancel();
        return;
      }

      if (Arrays.asList(60, 50, 40, 30, 25, 20, 15, 10, 5, 4, 3, 2, 1).contains(time)) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ENDING-IN")
                .replace("<countdown>", String.valueOf(time)));
      }

      time--;
    }
  }

  public class WaterCheckTask extends BukkitRunnable {

    @Override
    public void run() {
      if (getPlayers().size() <= 1) {
        return;
      }

      getBukkitPlayers().forEach(player -> {
        if (getPlayer(player) != null
            && getPlayer(player).getState() != ParkourPlayer.ParkourState.INGAME) {
          return;
        }

        if (PlayerUtil.isStandingOnLiquid(player)) {
          teleportToSpawnOrCheckpoint(player);
        } else if (PlayerUtil.isStandingOn(player, Material.STONE_PLATE) || PlayerUtil.isStandingOn(
            player, Material.IRON_PLATE) || PlayerUtil.isStandingOn(player, Material.WOOD_PLATE)) {
          ParkourPlayer parkourPlayer = getPlayer(player.getUniqueId());
          if (parkourPlayer != null) {
            boolean checkpoint = false;
            if (parkourPlayer.getLastCheckpoint() == null) {
              checkpoint = true;
              parkourPlayer.setLastCheckpoint(
                  CustomLocation.fromBukkitLocation(player.getLocation()));
            } else if (parkourPlayer.getLastCheckpoint() != null && !isSameLocation(
                player.getLocation(), parkourPlayer.getLastCheckpoint().toBukkitLocation())) {
              checkpoint = true;
              parkourPlayer.setLastCheckpoint(
                  CustomLocation.fromBukkitLocation(player.getLocation()));
            }
            if (checkpoint) {
              parkourPlayer.setCheckpointId(parkourPlayer.getCheckpointId() + 1);
              CC.sendMessage(player, getPlugin().getMessagesConfig().getConfig()
                  .getString("MESSAGES.EVENT.PARKOUR-CHECKPOINT")
                  .replace("<checkpoint>", String.valueOf(parkourPlayer.getCheckpointId()))
              );
            }
          }
        } else if (PlayerUtil.isStandingOn(player, Material.GOLD_PLATE)) {
          handleWin(player);
          end();
          cancel();
        }
      });
    }
  }

  private boolean isSameLocation(Location location, Location check) {
    return location.getWorld().getName().equalsIgnoreCase(check.getWorld().getName())
        && location.getBlockX() == check.getBlockX() && location.getBlockY() == check.getBlockY()
        && location.getBlockZ() == check.getBlockZ();
  }
}