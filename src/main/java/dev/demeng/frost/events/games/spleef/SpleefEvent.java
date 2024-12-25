package dev.demeng.frost.events.games.spleef;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.PlayerUtil;
import io.netty.util.internal.ConcurrentSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class SpleefEvent extends PracticeEvent<SpleefPlayer> {

  private final Map<UUID, SpleefPlayer> players = new HashMap<>();
  private final Set<Location> brokenBlocks = new ConcurrentSet<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  private SpleefGameTask gameTask;
  private WaterCheckTask waterCheckTask;

  public SpleefEvent() {
    super("Spleef");
  }

  @Override
  public Map<UUID, SpleefPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        getPlugin().getManagerHandler().getSpawnManager().getSpleefLocation());
  }

  @Override
  public void onStart() {
    gameTask = new SpleefGameTask();
    gameTask.runTaskTimer(getPlugin(), 0, 20);

    waterCheckTask = new WaterCheckTask();
    waterCheckTask.runTaskTimer(getPlugin(), 0, 10L);
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(),
        new SpleefPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      SpleefPlayer data = getPlayer(player);
      if (data.getState() == SpleefPlayer.State.PLAYING) {
        data.setState(SpleefPlayer.State.ELIMINATED);

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
      }

      if (getByState(SpleefPlayer.State.PLAYING).size() == 1) {
        Player winner = Bukkit.getPlayer(getByState(SpleefPlayer.State.PLAYING).get(0));
        if (winner != null) {
          handleWin(winner);
        }
        end();
      }
    };
  }

  public void cancelAll() {
    if (gameTask != null) {
      gameTask.cancel();
    }
    if (waterCheckTask != null) {
      waterCheckTask.cancel();
    }
  }

  public List<UUID> getByState(SpleefPlayer.State state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(SpleefPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class SpleefGameTask extends BukkitRunnable {

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

        for (SpleefPlayer spleefPlayer : getPlayers().values()) {
          spleefPlayer.setState(SpleefPlayer.State.PLAYING);

          Player player = Bukkit.getPlayer(spleefPlayer.getUuid());
          player.getInventory().clear();
          player.getInventory().setItem(0,
              new ItemBuilder(Material.DIAMOND_SPADE).enchantment(Enchantment.DIG_SPEED, 10)
                  .hideFlags().build());
        }
      }

      time--;
    }
  }

  @Getter
  @RequiredArgsConstructor
  public class WaterCheckTask extends BukkitRunnable {

    @Override
    public void run() {
      if (getPlayers().size() <= 1) {
        return;
      }

      getBukkitPlayers().forEach(player -> {
        if (getPlayer(player) != null
            && getPlayer(player).getState() != SpleefPlayer.State.PLAYING) {
          return;
        }
        if (PlayerUtil.isStandingOnLiquid(player)) {
          onDeath().accept(player);
        }
      });
    }
  }
}
