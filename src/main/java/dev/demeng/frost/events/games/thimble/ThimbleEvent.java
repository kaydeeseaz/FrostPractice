package dev.demeng.frost.events.games.thimble;

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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class ThimbleEvent extends PracticeEvent<ThimblePlayer> {

  private final Map<UUID, ThimblePlayer> players = new HashMap<>();
  private final Map<Location, Block> blocks = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  private WaterDropGameTask gameTask;
  private WaterDropCheckTask waterCheckTask;

  private List<UUID> visibility;
  private int round;

  public ThimbleEvent() {
    super("Thimble");
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        getPlugin().getManagerHandler().getSpawnManager().getThimbleLocation());
  }

  @Override
  public void onStart() {
    round = 0;
    visibility = new ArrayList<>();
    gameTask = new WaterDropGameTask();
    gameTask.runTaskTimerAsynchronously(getPlugin(), 0L, 20L);
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(),
        new ThimblePlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      ThimblePlayer data = getPlayer(player);
      if (data.getState() != ThimblePlayer.State.WAITING) {
        data.setState(ThimblePlayer.State.ELIMINATED);
        getPlayers().remove(player.getUniqueId());
        getPlugin().getManagerHandler().getEventManager().addSpectator(player,
            getPlugin().getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
            this);

        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.PLAYER-ELIMINATED")
            .replace("<eventName>", getName())
            .replace("<player>", player.getName())
        );

        CC.sendMessage(player,
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ELIMINATED")
                .replace("<eventName>", getName()));

        if (getByState(ThimblePlayer.State.JUMPING).size() == 1) {
          Player winner = Bukkit.getPlayer(getByState(ThimblePlayer.State.JUMPING).get(0));
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
      getPlugin().getManagerHandler().getItemManager().getParkourItems().stream()
          .filter(ItemManager.HotbarItem::isEnabled)
          .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack()));
      player.updateInventory();
    });
  }

  private void nextRound() {
    List<Player> roundPlayers = prepareNextRoundPlayers();
    for (Player player : roundPlayers) {
      getPlayer(player).setState(ThimblePlayer.State.JUMPING);
      getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> player.teleport(
          getPlugin().getManagerHandler().getSpawnManager().getThimbleGameLocation()
              .toBukkitLocation()));
    }
  }

  private List<Player> prepareNextRoundPlayers() {
    List<Player> waterDropPlayers = new ArrayList<>();
    if (getByState(ThimblePlayer.State.NEXT_ROUND).size() > 0) {
      for (UUID uuid : getByState(ThimblePlayer.State.NEXT_ROUND)) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
          waterDropPlayers.add(player);
        }
      }
    } else if (getByState(ThimblePlayer.State.JUMPING).size() > 0) {
      for (UUID uuid : getByState(ThimblePlayer.State.JUMPING)) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
          waterDropPlayers.add(player);
        }
      }
    }

    round++;

    return waterDropPlayers;
  }

  private Player getRandomPlayer() {
    List<Player> playersRandom = new ArrayList<>(getBukkitPlayers());
    Collections.shuffle(playersRandom);
    return playersRandom.get(ThreadLocalRandom.current().nextInt(playersRandom.size()));
  }

  private List<UUID> getByState(ThimblePlayer.State state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(ThimblePlayer::getUuid).collect(Collectors.toList());
  }

  public void cancelAll() {
    if (gameTask != null) {
      gameTask.cancel();
    }
    if (waterCheckTask != null) {
      waterCheckTask.cancel();
    }
  }

  private void spawnBlock(Player player) {
    Block block = player.getLocation().getBlock();
    block.setType(Material.WOOL);
    blocks.put(player.getLocation(), block);
    player.teleport(block.getLocation().add(0.0, 0.5, 0.0));
  }

  @Getter
  @RequiredArgsConstructor
  public class WaterDropGameTask extends BukkitRunnable {

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

        getPlayers().forEach((uuid, thimblePlayer) -> {
          thimblePlayer.setState(ThimblePlayer.State.JUMPING);
          giveItems(Bukkit.getPlayer(uuid));
        });

        waterCheckTask = new WaterDropCheckTask();
        waterCheckTask.runTaskTimerAsynchronously(getPlugin(), 0L, 10L);

        nextRound();
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

  public class WaterDropCheckTask extends BukkitRunnable {

    public void run() {
      if (getPlayers().size() <= 1) {
        cancel();
        return;
      }

      getBukkitPlayers().forEach(player -> {
        if (getPlayer(player) != null
            && getPlayer(player).getState() == ThimblePlayer.State.JUMPING) {
          if (PlayerUtil.isStandingOnLiquid(player)) {
            getPlayer(player.getUniqueId()).setState(ThimblePlayer.State.NEXT_ROUND);
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
              spawnBlock(player);
              player.teleport(getPlugin().getManagerHandler().getSpawnManager().getThimbleLocation()
                  .toBukkitLocation());
            });
          }
        }
      });

      if (getByState(ThimblePlayer.State.NEXT_ROUND).size() != 0 && getByState(
          ThimblePlayer.State.JUMPING).isEmpty()) {
        getPlugin().getServer().getScheduler()
            .runTaskAsynchronously(getPlugin(), ThimbleEvent.this::nextRound);
      }
    }
  }
}
