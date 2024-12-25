package dev.demeng.frost.events.games.skywars;

import static dev.demeng.frost.managers.PlayerManager.setKnockbackProfile;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.cuboid.Cuboid;
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
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class SkyWarsEvent extends PracticeEvent<SkyWarsPlayer> {

  private final Map<UUID, SkyWarsPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  private SkyWarsGameTask gameTask;
  private Cuboid cuboid;

  @Setter private List<CustomLocation> availableSpawns = new ArrayList<>();

  public SkyWarsEvent() {
    super("SkyWars");
    this.availableSpawns.addAll(
        getPlugin().getManagerHandler().getSpawnManager().getSkywarsLocations());
    this.setLimit(getAvailableSpawns().size());
  }

  public void setCuboid(Cuboid cuboid) {
    this.cuboid = cuboid;
  }

  @Override
  public Map<UUID, SkyWarsPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return getPlugin().getManagerHandler().getSpawnManager().getSkywarsLocations();
  }

  @Override
  public void onStart() {
    gameTask = new SkyWarsGameTask();
    gameTask.runTaskTimerAsynchronously(getPlugin(), 0, 20L);
  }

  public void cancelAll() {
    if (gameTask != null) {
      gameTask.cancel();
    }
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(),
        new SkyWarsPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      SkyWarsPlayer data = getPlayer(player);
      if (data.getState() != SkyWarsPlayer.SkyWarsState.FIGHTING) {
        return;
      }

      Player killer = player.getKiller();
      data.setState(SkyWarsPlayer.SkyWarsState.ELIMINATED);

      getPlayers().remove(player.getUniqueId());
      getPlugin().getManagerHandler().getEventManager().addSpectator(player,
          getPlugin().getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
          this);

      sendMessage(
          ChatColor.RED + player.getName() + ChatColor.GRAY + " was eliminated" + (killer == null
              ? "." : " by " + ChatColor.RED + killer.getName()) + ChatColor.GRAY + ".");
      player.sendMessage(" ");
      player.sendMessage(
          ChatColor.RED + "You have been eliminated from the event. Better luck next time!");
      player.sendMessage(" ");

      if (getByState(SkyWarsPlayer.SkyWarsState.FIGHTING).size() == 1) {
        Player winner = Bukkit.getPlayer(getByState(SkyWarsPlayer.SkyWarsState.FIGHTING).get(0));
        if (winner != null) {
          handleWin(winner);
        }
        end();
        cancelAll();
      }
    };
  }

  private Player getRandomPlayer() {
    if (getByState(SkyWarsPlayer.SkyWarsState.FIGHTING).size() == 0) {
      return null;
    }

    List<UUID> fighting = getByState(SkyWarsPlayer.SkyWarsState.FIGHTING);
    Collections.shuffle(fighting);
    UUID uuid = fighting.get(ThreadLocalRandom.current().nextInt(fighting.size()));

    return getPlugin().getServer().getPlayer(uuid);
  }

  public List<UUID> getByState(SkyWarsPlayer.SkyWarsState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(SkyWarsPlayer::getUuid).collect(Collectors.toList());
  }

  public void resetSpawns() {
    this.availableSpawns.clear();
    this.availableSpawns.addAll(
        getPlugin().getManagerHandler().getSpawnManager().getSkywarsLocations());
  }

  private CustomLocation getRandomSpawn() {
    for (CustomLocation spawn : getSpawnLocations()) {
      if (spawn != null) {
        availableSpawns.add(spawn);
      }
    }

    int random = (int) (Math.random() * availableSpawns.size());
    return availableSpawns.get(random);
  }

  @Getter
  @RequiredArgsConstructor
  public class SkyWarsGameTask extends BukkitRunnable {

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

        getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
          getPlayers().forEach((uuid, skyWarsPlayer) -> {
            skyWarsPlayer.setState(SkyWarsPlayer.SkyWarsState.FIGHTING);

            Player player = Bukkit.getPlayer(uuid);
            CustomLocation spawn = getRandomSpawn();
            player.teleport(spawn.toBukkitLocation());
            availableSpawns.remove(spawn);

            getKitOptional().ifPresent(kit -> {
              kit.applyKit(player);
              setKnockbackProfile(player, kit.getKbProfile());
            });
          });
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
        Player winner = Bukkit.getPlayer(getByState(SkyWarsPlayer.SkyWarsState.FIGHTING).get(0));
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
}