package dev.demeng.frost.events.games.knockout;

import static dev.demeng.frost.managers.PlayerManager.setKnockbackProfile;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.PlayerUtil;
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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class KnockoutEvent extends PracticeEvent<KnockoutPlayer> {

  private final Map<UUID, KnockoutPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  private KnockoutGameTask gameTask;

  public KnockoutEvent() {
    super("Knockout");
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return getPlugin().getManagerHandler().getSpawnManager().getKnockoutLocations();
  }

  @Override
  public void onStart() {
    gameTask = new KnockoutGameTask();
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
        new KnockoutPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      KnockoutPlayer data = getPlayer(player);
      if (data.getState() != KnockoutPlayer.KnockoutState.FIGHTING) {
        return;
      }

      data.setState(KnockoutPlayer.KnockoutState.ELIMINATED);

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

      if (getByState(KnockoutPlayer.KnockoutState.FIGHTING).size() == 1) {
        Player winner = Bukkit.getPlayer(getByState(KnockoutPlayer.KnockoutState.FIGHTING).get(0));
        if (winner != null) {
          handleWin(winner);
        }

        end();
        cancelAll();
      }
    };
  }

  private Player getRandomPlayer() {
    if (getByState(KnockoutPlayer.KnockoutState.FIGHTING).size() == 0) {
      return null;
    }

    List<UUID> fighting = getByState(KnockoutPlayer.KnockoutState.FIGHTING);
    Collections.shuffle(fighting);
    UUID uuid = fighting.get(ThreadLocalRandom.current().nextInt(fighting.size()));

    return getPlugin().getServer().getPlayer(uuid);
  }

  public List<UUID> getByState(KnockoutPlayer.KnockoutState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(KnockoutPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class KnockoutGameTask extends BukkitRunnable {

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
        getPlayers().forEach((uuid, knockoutPlayer) -> {
          knockoutPlayer.setState(KnockoutPlayer.KnockoutState.FIGHTING);

          Player player = Bukkit.getPlayer(uuid);
          player.teleport(getSpawnLocations().remove(
              ThreadLocalRandom.current().nextInt(getSpawnLocations().size())).toBukkitLocation());
          getKitOptional().ifPresent(kit -> {
            setKnockbackProfile(player, "default");
          });
          getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
            PlayerUtil.clearPlayer(player, false);
            player.getInventory().addItem(new ItemBuilder(Material.STICK).name("&bKnockout Stick")
                .enchantment(Enchantment.LUCK).enchantment(Enchantment.KNOCKBACK, 1).build());
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
        Player winner = Bukkit.getPlayer(getByState(KnockoutPlayer.KnockoutState.FIGHTING).get(0));
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
