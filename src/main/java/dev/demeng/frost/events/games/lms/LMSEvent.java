package dev.demeng.frost.events.games.lms;

import static dev.demeng.frost.managers.PlayerManager.setKnockbackProfile;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CustomLocation;
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
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class LMSEvent extends PracticeEvent<LMSPlayer> {

  private final Map<UUID, LMSPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  private LMSGameTask gameTask;

  public LMSEvent() {
    super("LMS");
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return getPlugin().getManagerHandler().getSpawnManager().getLmsLocations();
  }

  @Override
  public void onStart() {
    gameTask = new LMSGameTask();
    gameTask.runTaskTimerAsynchronously(getPlugin(), 0, 20L);
  }

  public void cancelAll() {
    if (gameTask != null) {
      gameTask.cancel();
    }
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(), new LMSPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      LMSPlayer data = getPlayer(player);
      if (data.getState() != LMSPlayer.LMSState.FIGHTING) {
        return;
      }

      Player killer = player.getKiller();
      data.setState(LMSPlayer.LMSState.ELIMINATED);

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

      if (getByState(LMSPlayer.LMSState.FIGHTING).size() == 1) {
        Player winner = Bukkit.getPlayer(getByState(LMSPlayer.LMSState.FIGHTING).get(0));
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancelAll();
      }
    };
  }

  private Player getRandomPlayer() {
    if (getByState(LMSPlayer.LMSState.FIGHTING).size() == 0) {
      return null;
    }

    List<UUID> fighting = getByState(LMSPlayer.LMSState.FIGHTING);
    Collections.shuffle(fighting);
    UUID uuid = fighting.get(ThreadLocalRandom.current().nextInt(fighting.size()));

    return getPlugin().getServer().getPlayer(uuid);
  }

  public List<UUID> getByState(LMSPlayer.LMSState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(LMSPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class LMSGameTask extends BukkitRunnable {

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
          getPlayers().forEach((uuid, lmsPlayer) -> {
            lmsPlayer.setState(LMSPlayer.LMSState.FIGHTING);

            Player player = Bukkit.getPlayer(uuid);
            player.teleport(getSpawnLocations().remove(
                    ThreadLocalRandom.current().nextInt(getSpawnLocations().size()))
                .toBukkitLocation());
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
        Player winner = Bukkit.getPlayer(getByState(LMSPlayer.LMSState.FIGHTING).get(0));
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