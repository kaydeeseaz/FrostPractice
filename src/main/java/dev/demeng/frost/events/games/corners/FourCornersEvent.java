package dev.demeng.frost.events.games.corners;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.cuboid.Cuboid;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class FourCornersEvent extends PracticeEvent<FourCornersPlayer> {

  private final Map<UUID, FourCornersPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  private CornersGameTask gameTask;
  private MoveTask moveTask;
  private RemoveBlocksTask removeBlocksTask;
  private Map<Location, ItemStack> blocks;
  private int seconds, randomWool, round;
  private boolean running = false;
  private Cuboid zone;

  public FourCornersEvent() {
    super("4Corners");
  }

  @Override
  public Map<UUID, FourCornersPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        getPlugin().getManagerHandler().getSpawnManager().getCornersLocation());
  }

  @Override
  public void onStart() {
    seconds = 11;
    round = 1;
    gameTask = new CornersGameTask();
    gameTask.runTaskTimerAsynchronously(getPlugin(), 0L, 20L);
    blocks = new HashMap<>();
    zone = new Cuboid(
        getPlugin().getManagerHandler().getSpawnManager().getCornersMin().toBukkitLocation(),
        getPlugin().getManagerHandler().getSpawnManager().getCornersMax().toBukkitLocation());
  }

  private void cancelAll() {
    if (gameTask != null) {
      gameTask.cancel();
    }
    if (moveTask != null) {
      moveTask.cancel();
    }
    if (removeBlocksTask != null) {
      removeBlocksTask.cancel();
    }

    running = false;
    zone = null;
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(),
        new FourCornersPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {

    return player -> {
      FourCornersPlayer data = getPlayer(player);

      if (data.getState() != FourCornersPlayer.FourCornerState.INGAME) {
        return;
      }

      data.setState(FourCornersPlayer.FourCornerState.ELIMINATED);

      getPlayers().remove(player.getUniqueId());
      getPlugin().getManagerHandler().getEventManager().addSpectator(player,
          getPlugin().getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
          this);

      sendMessage(
          getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.PLAYER-ELIMINATED")
              .replace("<eventName>", this.getName())
              .replace("<player>", player.getName())
      );

      CC.sendMessage(player,
          getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ELIMINATED")
              .replace("<eventName>", this.getName()));

      if (getByState(FourCornersPlayer.FourCornerState.INGAME).size() == 1) {
        Player winner = Bukkit.getPlayer(
            getByState(FourCornersPlayer.FourCornerState.INGAME).get(0));
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancelAll();

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
          blocks.forEach(((location, stack) -> location.getBlock()
              .setTypeIdAndData(stack.getTypeId(), (byte) stack.getDurability(), true)));
          if (blocks.size() > 0) {
            blocks.clear();
          }
        }, 40L);
      }
    };
  }

  public List<UUID> getByState(FourCornersPlayer.FourCornerState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(FourCornersPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class CornersGameTask extends BukkitRunnable {

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

        new BukkitRunnable() {
          @Override
          public void run() {
            getBukkitPlayers().stream()
                .filter(player -> getPlayers().containsKey(player.getUniqueId())).forEach(
                    player -> player.teleport(
                        getPlugin().getManagerHandler().getSpawnManager().getCornersLocation()
                            .toBukkitLocation()));
          }
        }.runTask(getPlugin());

        getPlayers().values()
            .forEach(player -> player.setState(FourCornersPlayer.FourCornerState.INGAME));
        getBukkitPlayers().forEach(player -> player.getInventory().clear());

        moveTask = new MoveTask();
        moveTask.runTaskTimer(getPlugin(), 0, 1L);

        removeBlocksTask = new RemoveBlocksTask();
        removeBlocksTask.runTaskTimer(getPlugin(), 0L, 20L);
        running = true;
      } else if (time <= 0) {
        Player winner = getRandomPlayer();
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancelAll();
        cancel();
        return;
      }

      if (getPlayers().size() == 1) {
        Player winner = Bukkit.getPlayer(
            getByState(FourCornersPlayer.FourCornerState.INGAME).get(0));
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancelAll();
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

  private Player getRandomPlayer() {
    if (getByState(FourCornersPlayer.FourCornerState.INGAME).size() == 0) {
      return null;
    }

    List<UUID> fighting = getByState(FourCornersPlayer.FourCornerState.INGAME);
    Collections.shuffle(fighting);
    UUID uuid = fighting.get(ThreadLocalRandom.current().nextInt(fighting.size()));

    return getPlugin().getServer().getPlayer(uuid);
  }

  @RequiredArgsConstructor
  private class MoveTask extends BukkitRunnable {

    @Override
    public void run() {
      getBukkitPlayers().forEach(player -> {
        if (getPlayer(player.getUniqueId()) != null && getPlayer(player.getUniqueId()).getState()
            == FourCornersPlayer.FourCornerState.INGAME) {
          if (getPlayers().size() <= 1) {
            return;
          }
          if (getPlayers().containsKey(player.getUniqueId())) {
            if (PlayerUtil.isStandingOnLiquid(player)) {
              onDeath().accept(player);
            }
          }
        }
      });
    }
  }

  @RequiredArgsConstructor
  private class RemoveBlocksTask extends BukkitRunnable {

    @Override
    public void run() {
      if (!running) {
        return;
      }

      seconds--;

      if (seconds <= 0) {
        running = false;
        handleRemoveBridges(true);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
          handleRemoveBridges(false);

          Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            blocks.forEach(((location, stack) -> location.getBlock()
                .setTypeIdAndData(stack.getTypeId(), (byte) stack.getDurability(), true)));
            if (blocks.size() > 0) {
              blocks.clear();
            }
          }, 60L);

          Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            round++;
            seconds = 11;
            running = true;
          }, 100L);
        }, 60L);

        return;
      }

      if (Arrays.asList(10, 5, 4, 3, 2, 1).contains(seconds)) {
        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.TYPES.BRIDGES.DROPPING")
            .replace("<time>", String.valueOf(seconds))
            .replace("<round>", String.valueOf(round)));
      }
    }
  }

  private void handleRemoveBridges(boolean bridges) {
    randomWool = getRandomWool();

    if (zone != null) {
      zone.forEach(block -> {
        if (bridges) {
          if (!block.getType().equals(Material.WOOL)) {
            blocks.put(block.getLocation(), new ItemStack(block.getType(), 1, block.getData()));
            block.setType(Material.AIR);
          }
        } else {
          if (block.getType().equals(Material.WOOL) && block.getData() == (byte) randomWool) {
            blocks.put(block.getLocation(), new ItemStack(block.getType(), 1, (short) randomWool));
            block.setType(Material.AIR);
            block.getLocation().getWorld().strikeLightningEffect(block.getLocation());
          }
        }
      });
    }

    if (!bridges) {
      sendMessage(getPlugin().getMessagesConfig().getConfig()
          .getString("MESSAGES.EVENT.TYPES.BRIDGES.DROPPED")
          .replace("<wool>", (randomWool == 14 ? "&cRed"
              : randomWool == 11 ? "&9Blue" : randomWool == 5 ? "&aGreen" : "&eYellow"))
          .replace("<round>", String.valueOf(round)));
    }
  }

  private int getRandomWool() {
    List<Integer> wools = Arrays.asList(14, 11, 5, 4);
    return wools.get(ThreadLocalRandom.current().nextInt(wools.size()));
  }
}