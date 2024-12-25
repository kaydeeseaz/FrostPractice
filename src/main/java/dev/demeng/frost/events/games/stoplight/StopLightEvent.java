package dev.demeng.frost.events.games.stoplight;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.Callback;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@Setter
public class StopLightEvent extends PracticeEvent<StopLightPlayer> {

  private Map<UUID, StopLightPlayer> players = new HashMap<>();
  private List<UUID> movingPlayers = new ArrayList<>();
  private EventCountdown eventCountdown = new EventCountdown(this, 2);

  private Random random = new Random();

  private StopLightGameTask gameTask;
  private State current;

  public StopLightEvent() {
    super("StopLight");
  }

  @Override
  public Map<UUID, StopLightPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return getPlugin().getManagerHandler().getSpawnManager().getStoplightLocations();
  }

  @Override
  public void onStart() {
    current = State.GO;
    gameTask = new StopLightGameTask();
    gameTask.runTaskTimerAsynchronously(getPlugin(), 0L, 20L);

    movingPlayers.clear();
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> {
      players.put(player.getUniqueId(), new StopLightPlayer(player.getUniqueId(), this));
      player.teleport(getPlugin().getManagerHandler().getSpawnManager().getStoplightLocation()
          .toBukkitLocation());
    };
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> sendMessage(
        "&8[&b&lEvent&8] &c" + player.getDisplayName() + " has left the event.");
  }

  private void giveItems(Player player) {
    getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
      for (int i = 0; i <= 8; i++) {
        player.getInventory().setItem(i,
            new ItemBuilder(Material.WOOL).name(current == State.GO ? "&a&lGO GO GO!" : "&c&lSTOP!")
                .durability(current == State.GO ? 5 : 14).build());
      }

      player.updateInventory();
    });
  }

  private List<UUID> getByState(StopLightPlayer.State state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(StopLightPlayer::getUuid).collect(Collectors.toList());
  }

  private void getRandomSpawn(Callback<Location> locationCallback) {
    if (getSpawnLocations().size() == 0) {
      getPlugin().getLogger().warning("No spawn locations found!");
      return;
    }

    int rand = random.nextInt(getSpawnLocations().size());
    locationCallback.done(getSpawnLocations().get(rand).toBukkitLocation());
  }

  private Player getRandomPlayer() {
    List<Player> playersRandom = new ArrayList<>(getBukkitPlayers());
    Collections.shuffle(playersRandom);
    return playersRandom.get(ThreadLocalRandom.current().nextInt(playersRandom.size()));
  }

  public enum State {
    GO, STOP
  }

  @Getter
  @RequiredArgsConstructor
  public class StopLightGameTask extends BukkitRunnable {

    private boolean teleported = false;
    private int startTime = 5;
    private int roundTime = random.nextInt(2) + 1;
    private int stopTime = random.nextInt(3) + 2;
    private int stopMovingTime = stopTime - 2;

    public void run() {
      if (getPlayers().size() == 1) {
        Player winner = Bukkit.getPlayer(getByState(StopLightPlayer.State.INGAME).get(0));
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancel();
        return;
      }

      if (!teleported) {
        for (StopLightPlayer stopLightPlayer : getPlayers().values()) {
          stopLightPlayer.setState(StopLightPlayer.State.INGAME);
          getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
            Player player = Bukkit.getPlayer(stopLightPlayer.getUuid());
            player.setGameMode(GameMode.ADVENTURE);
            getRandomSpawn(player::teleport);
          });
        }
        teleported = true;
      }

      if (startTime > 0) {
        if (startTime == 1) {
          sendMessage(CC.color("&7Starting in &b1 &7second!"));
        } else {
          sendMessage(CC.color("&7Starting in &b" + startTime + " &7seconds!"));
        }
        startTime--;
        return;
      }

      if (startTime == 0) {
        for (Player player : getBukkitPlayers()) {
          getRandomSpawn(player::teleport);
          giveItems(player);
        }
        startTime--;
      }

      if (roundTime > 0) {
        current = State.GO;
        roundTime--;
      } else {
        if (current == State.GO) {
          current = State.STOP;
          for (Player player : getBukkitPlayers()) {
            giveItems(player);
            player.playSound(player.getLocation(), Sound.ANVIL_LAND, 0.5f, 1.0f);
          }
        }

        stopTime--;
        if (stopTime <= stopMovingTime) {
          movingPlayers.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
              getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                players.remove(uuid);
                player.getLocation().getWorld().strikeLightningEffect(player.getLocation());
                getPlugin().getManagerHandler().getEventManager().addSpectator(player,
                    getPlugin().getManagerHandler().getPlayerManager()
                        .getPlayerData(player.getUniqueId()),
                    StopLightEvent.this
                );

                sendMessage(getPlugin().getMessagesConfig().getConfig()
                    .getString("MESSAGES.EVENT.PLAYER-ELIMINATED")
                    .replace("<eventName>", getName())
                    .replace("<player>", player.getName())
                );
              });
            }
          });
        }

        movingPlayers.clear();

        if (stopTime <= 0) {
          current = State.GO;
          for (Player player : getBukkitPlayers()) {
            giveItems(player);
          }
          roundTime = random.nextInt(2) + 1;
          stopTime = random.nextInt(3) + 2;
          stopMovingTime = stopTime - 2;
        }
      }

      int playersSize = getPlayers().size();
      if (playersSize == 1) {
        Player winner = getRandomPlayer();
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancel();
      } else if (playersSize == 0) {
        end();
        cancel();
      }
    }
  }
}
