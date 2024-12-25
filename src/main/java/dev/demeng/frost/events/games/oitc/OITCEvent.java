package dev.demeng.frost.events.games.oitc;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.PlayerUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class OITCEvent extends PracticeEvent<OITCPlayer> {

  private final Map<UUID, OITCPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 3);

  @Getter private OITCGameTask gameTask = null;
  private List<CustomLocation> respawnLocations;

  @Getter @Setter private boolean running = false;

  public OITCEvent() {
    super("OITC");
  }

  @Override
  public Map<UUID, OITCPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        this.getPlugin().getManagerHandler().getSpawnManager().getOitcLocation());
  }

  @Override
  public void onStart() {
    this.respawnLocations = new ArrayList<>();
    this.gameTask = new OITCGameTask();
    this.running = true;
    this.gameTask.runTaskTimerAsynchronously(getPlugin(), 0, 20L);
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(), new OITCPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      OITCPlayer data = getPlayer(player);
      if (data == null || data.getState() == OITCPlayer.OITCState.WAITING) {
        return;
      }

      if (data.getState() == OITCPlayer.OITCState.FIGHTING
          || data.getState() == OITCPlayer.OITCState.PREPARING
          || data.getState() == OITCPlayer.OITCState.RESPAWNING) {
        String deathMessage =
            ChatColor.RED + player.getName() + "&8[&b" + data.getScore() + "&8] " + ChatColor.GRAY
                + "was eliminated from the game.";
        if (data.getLastKiller() != null) {
          OITCPlayer killerData = data.getLastKiller();
          Player killer = Bukkit.getPlayer(killerData.getUuid());

          int count = killerData.getScore() + 1;
          killerData.setScore(count);

          if (!killer.getInventory().contains(Material.ARROW)) {
            killer.getInventory().setItem(8, new ItemStack(Material.ARROW, 1));
          } else {
            killer.getInventory().getItem(8)
                .setAmount(killer.getInventory().getItem(8).getAmount() + 1);
          }

          killer.updateInventory();
          killer.resetMaxHealth();
          killer.setHealth(player.getMaxHealth());
          killer.playSound(killer.getLocation(), Sound.NOTE_PLING, 1F, 1F);

          data.setLastKiller(null);

          deathMessage =
              ChatColor.RED + player.getName() + "&8[&c" + data.getScore() + "&8] " + ChatColor.GRAY
                  + "was killed by " + ChatColor.GREEN + killer.getName() + "&8[&a" + count + "&8]";

          if (count == 20) {
            handleWin(killer);
            gameTask.cancel();
            end();
          }
        }

        if (data.getLastKiller() == null) {
          BukkitTask respawnTask = new RespawnTask(player, data).runTaskTimerAsynchronously(
              getPlugin(), 0L, 20L);
          data.setRespawnTask(respawnTask);
        }

        sendMessage(deathMessage);
      }
    };
  }

  public void teleportNextLocation(Player player) {
    new BukkitRunnable() {
      @Override
      public void run() {
        player.teleport(getGameLocations().remove(
            ThreadLocalRandom.current().nextInt(getGameLocations().size())).toBukkitLocation());
      }
    }.runTask(getPlugin());
  }

  private List<CustomLocation> getGameLocations() {
    if (this.respawnLocations != null && this.respawnLocations.size() == 0) {
      this.respawnLocations.addAll(
          this.getPlugin().getManagerHandler().getSpawnManager().getOitcSpawnpoints());
    }

    return this.respawnLocations;
  }

  private void giveRespawnItems(Player player) {
    Bukkit.getScheduler().runTask(this.getPlugin(), () -> {
      PlayerUtil.clearPlayer(player, false);
      player.getInventory().setItem(0, new ItemStack(Material.WOOD_SWORD));
      player.getInventory().setItem(1, new ItemStack(Material.BOW));
      player.getInventory().setItem(8, new ItemStack(Material.ARROW));
      player.updateInventory();
    });
  }

  private Player getWinnerPlayer() {
    return getByState(OITCPlayer.OITCState.FIGHTING).size() == 0 ? null
        : Bukkit.getPlayer(sortedScores().get(0).getUuid());
  }

  private List<UUID> getByState(OITCPlayer.OITCState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(OITCPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class RespawnTask extends BukkitRunnable {

    private final Player player;
    private final OITCPlayer oitcPlayer;
    private int time = 3;

    @Override
    public void run() {
      if (!running) {
        cancel();
        return;
      }

      if (time > 0) {
        Bukkit.getScheduler().runTask(getPlugin(), () -> player.teleport(
            getPlugin().getManagerHandler().getSpawnManager().getOitcLocation()
                .toBukkitLocation()));
        CC.sendMessage(player,
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.OITC-RESPAWNING")
                .replace("<time>", String.valueOf(time)));
      }

      if (time == 3) {
        Bukkit.getScheduler().runTask(getPlugin(), () -> {
          PlayerUtil.clearPlayer(player, false);
          getBukkitPlayers().forEach(member -> PlayerUtil.hideOrShowPlayer(member, player, true));
          getBukkitPlayers().forEach(online -> PlayerUtil.hideOrShowPlayer(player, online, true));
          player.setGameMode(GameMode.ADVENTURE);
        });
        oitcPlayer.setState(OITCPlayer.OITCState.RESPAWNING);
      } else if (time <= 0) {
        CC.sendMessage(player,
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.OITC-RESPAWNED"));
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
          giveRespawnItems(player);
          player.teleport(getGameLocations().remove(
              ThreadLocalRandom.current().nextInt(getGameLocations().size())).toBukkitLocation());

          getBukkitPlayers().forEach(member -> PlayerUtil.hideOrShowPlayer(member, player, false));
          getBukkitPlayers().forEach(online -> PlayerUtil.hideOrShowPlayer(player, online, false));
        }, 2L);
        oitcPlayer.setState(OITCPlayer.OITCState.FIGHTING);
        cancel();
      }
      time--;
    }
  }

  @Getter
  @RequiredArgsConstructor
  public class OITCGameTask extends BukkitRunnable {

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

        getPlayers().values().forEach(player -> {
          player.setScore(0);
          player.setState(OITCPlayer.OITCState.FIGHTING);
        });

        getBukkitPlayers().forEach(player -> {
          OITCPlayer oitcPlayer = getPlayer(player.getUniqueId());
          if (oitcPlayer != null) {
            teleportNextLocation(player);
            giveRespawnItems(player);
          }
        });
      } else if (time <= 0) {
        Player winner = getWinnerPlayer();
        if (winner != null) {
          handleWin(winner);
        }

        gameTask.cancel();
        end();
        cancel();
        return;
      }

      if (getPlayers().size() == 1) {
        Player winner = Bukkit.getPlayer(getByState(OITCPlayer.OITCState.FIGHTING).get(0));
        if (winner != null) {
          handleWin(winner);
        }
        cancel();
        end();
      }

      if (Arrays.asList(60, 50, 40, 30, 25, 20, 15, 10, 5, 4, 3, 2, 1).contains(time)) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ENDING-IN")
                .replace("<countdown>", String.valueOf(time)));
      }

      time--;
    }
  }

  public List<OITCPlayer> sortedScores() {
    List<OITCPlayer> list = new ArrayList<>(this.players.values());
    list.sort(new SortComparator().reversed());
    return list;
  }

  private class SortComparator implements Comparator<OITCPlayer> {

    @Override
    public int compare(OITCPlayer p1, OITCPlayer p2) {
      return Integer.compare(p1.getScore(), p2.getScore());
    }
  }
}