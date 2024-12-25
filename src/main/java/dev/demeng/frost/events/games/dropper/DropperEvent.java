package dev.demeng.frost.events.games.dropper;

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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class DropperEvent extends PracticeEvent<DropperPlayer> {

  private final Map<UUID, DropperPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  private DropperGameTask gameTask;
  private DropperCheckTask dropperCheckTask;

  private List<UUID> visibility;

  public DropperEvent() {
    super("Dropper");
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return getPlugin().getManagerHandler().getSpawnManager().getDropperMaps();
  }

  @Override
  public void onStart() {
    visibility = new ArrayList<>();
    gameTask = new DropperGameTask();
    gameTask.runTaskTimerAsynchronously(getPlugin(), 0L, 20L);
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> {
      players.put(player.getUniqueId(), new DropperPlayer(player.getUniqueId(), this));
      player.teleport(getPlugin().getManagerHandler().getSpawnManager().getDropperLocation()
          .toBukkitLocation());
    };
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      DropperPlayer data = getPlayer(player);
      if (data == null || data.getState() == DropperPlayer.State.WAITING) {
        return;
      }

      if (data.getState() == DropperPlayer.State.PLAYING) {
        player.spigot().respawn();
        giveItems(player);

        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(),
            () -> player.teleport(getSpawnLocations().get(data.getPhase()).toBukkitLocation()), 1L);

        if (getPlayersInEvent().size() == 1) {
          Player winner = Bukkit.getPlayer(getPlayersInEvent().get(0));
          if (winner != null) {
            handleWin(winner);
          }

          end();
        }
      }
    };
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

  private void giveItems(Player player) {
    getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
      PlayerUtil.clearPlayer(player, false);
      for (ItemManager.HotbarItem item : getPlugin().getManagerHandler().getItemManager()
          .getParkourItems()) {
        if (item.isEnabled()) {
          player.getInventory().setItem(item.getSlot(), item.getItemStack());
        }
      }

      player.updateInventory();
    });
  }

  private Player getRandomPlayer() {
    List<Player> playersRandom = new ArrayList<>(getBukkitPlayers());
    Collections.shuffle(playersRandom);

    return playersRandom.get(ThreadLocalRandom.current().nextInt(playersRandom.size()));
  }

  private List<UUID> getPlayersInEvent() {
    List<UUID> list = new ArrayList<>();
    for (DropperPlayer player : players.values()) {
      if (player.getState() == DropperPlayer.State.PLAYING) {
        UUID uuid = player.getUuid();
        list.add(uuid);
      }
    }

    return list;
  }

  public void cancelAll() {
    if (gameTask != null) {
      gameTask.cancel();
    }
    if (dropperCheckTask != null) {
      dropperCheckTask.cancel();
    }
  }

  @Getter
  @RequiredArgsConstructor
  public class DropperGameTask extends BukkitRunnable {

    private int time = 303;

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

        dropperCheckTask = new DropperCheckTask();
        dropperCheckTask.runTaskTimerAsynchronously(getPlugin(), 0L, 10L);

        players.forEach((uuid, dropperPlayer) -> {
          Player player = Bukkit.getPlayer(uuid);
          if (player != null) {
            dropperPlayer.setState(DropperPlayer.State.PLAYING);
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
              giveItems(Bukkit.getPlayer(uuid));
              player.teleport(
                  getPlugin().getManagerHandler().getSpawnManager().getDropperMaps().get(0)
                      .toBukkitLocation());
            });
          }
        });
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
        Player winner = getRandomPlayer();
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

      --time;
    }
  }

  public class DropperCheckTask extends BukkitRunnable {

    public void run() {
      if (getPlayers().size() <= 1) {
        cancel();
        return;
      }

      getBukkitPlayers().forEach(player -> {
        if (getPlayer(player) != null
            && getPlayer(player).getState() == DropperPlayer.State.PLAYING
            && PlayerUtil.isStandingOnLiquid(player)) {
          DropperPlayer dropperPlayer = getPlayer(player);
          dropperPlayer.setPhase(dropperPlayer.getPhase() + 1);

          if (dropperPlayer.getPhase() >= getSpawnLocations().size()) {
            handleWin(player);
            end();
            cancel();
            return;
          }

          player.teleport(getSpawnLocations().get(dropperPlayer.getPhase()).toBukkitLocation());
        }
      });
    }
  }
}
