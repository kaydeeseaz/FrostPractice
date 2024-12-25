package dev.demeng.frost.events.games.sumo;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.managers.PlayerManager;
import dev.demeng.frost.user.effects.SpecialEffects;
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
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SumoEvent extends PracticeEvent<SumoPlayer> {

  private final Map<UUID, SumoPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  @Getter final List<Player> fighting = new ArrayList<>();
  @Getter private WaterCheckTask waterCheckTask;
  @Getter @Setter private int round;
  @Getter private Player playerA;
  @Getter private Player playerB;

  public SumoEvent() {
    super("Sumo");
  }

  @Override
  public Map<UUID, SumoPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        getPlugin().getManagerHandler().getSpawnManager().getSumoLocation());
  }

  @Override
  public void onStart() {
    round = 0;
    waterCheckTask = new WaterCheckTask();
    waterCheckTask.runTaskTimer(getPlugin(), 0, 10L);
    selectPlayers();
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(), new SumoPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      SumoPlayer data = getPlayer(player);
      if (data == null || data.getFighting() == null) {
        return;
      }

      if (data.getState() == SumoPlayer.SumoState.FIGHTING
          || data.getState() == SumoPlayer.SumoState.PREPARING) {
        SumoPlayer killerData = data.getFighting();
        Player killer = getPlugin().getServer().getPlayer(killerData.getUuid());

        data.getFightTask().cancel();
        killerData.getFightTask().cancel();

        data.setState(SumoPlayer.SumoState.ELIMINATED);
        killerData.setState(SumoPlayer.SumoState.WAITING);

        SpecialEffects specialEffect = getPlugin().getManagerHandler().getPlayerManager()
            .getPlayerData(player.getUniqueId()).getPlayerSettings().getSpecialEffect();
        if (specialEffect != null && !specialEffect.getName().equalsIgnoreCase("none")) {
          getBukkitPlayers().forEach(bukkitPlayer -> specialEffect.getCallable()
              .call(player, getBukkitPlayers().toArray(new Player[0])));
        }

        getPlayers().remove(player.getUniqueId());
        getPlugin().getManagerHandler().getEventManager().addSpectator(player,
            getPlugin().getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
            this);

        PlayerUtil.clearPlayer(killer, false);
        getPlugin().getManagerHandler().getPlayerManager().giveLobbyItems(killer);

        if (getSpawnLocations().size() == 1) {
          player.teleport(getSpawnLocations().get(0).toBukkitLocation());
          killer.teleport(getSpawnLocations().get(0).toBukkitLocation());
        }

        if (killer == null) {
          sendMessage(getPlugin().getMessagesConfig().getConfig()
              .getString("MESSAGES.EVENT.PLAYER-ELIMINATED")
              .replace("<eventName>", this.getName())
              .replace("<round>", String.valueOf(round))
              .replace("<player>", player.getName())
          );
        } else {
          sendMessage(getPlugin().getMessagesConfig().getConfig()
              .getString("MESSAGES.EVENT.PLAYER-ELIMINATED-BY-KILLER")
              .replace("<eventName>", this.getName())
              .replace("<round>", String.valueOf(round))
              .replace("<player>", player.getName())
              .replace("<killer>", killer.getName())
          );
        }

        CC.sendMessage(player,
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ELIMINATED")
                .replace("<eventName>", this.getName()));

        if (getByState(SumoPlayer.SumoState.WAITING).size() == 1) {
          Player winner = Bukkit.getPlayer(getByState(SumoPlayer.SumoState.WAITING).get(0));
          handleWin(winner);
          fighting.clear();
          end();
        } else {
          getPlugin().getServer().getScheduler()
              .runTaskLater(getPlugin(), this::selectPlayers, 3 * 20);
        }
      }
    };
  }

  private CustomLocation[] getSumoLocations() {
    CustomLocation[] array = new CustomLocation[2];
    array[0] = getPlugin().getManagerHandler().getSpawnManager().getSumoFirst();
    array[1] = getPlugin().getManagerHandler().getSpawnManager().getSumoSecond();
    return array;
  }

  private void selectPlayers() {
    if (getByState(SumoPlayer.SumoState.WAITING).size() == 1) {
      Player winner = Bukkit.getPlayer(getByState(SumoPlayer.SumoState.WAITING).get(0));
      handleWin(winner);
      fighting.clear();
      end();

      return;
    }

    Player picked1 = getRandomPlayer();
    Player picked2 = getRandomPlayer();
    if (picked1 == null || picked2 == null) {
      selectPlayers();
      return;
    }

    fighting.clear();

    SumoPlayer picked1Data = getPlayer(picked1);
    SumoPlayer picked2Data = getPlayer(picked2);
    picked1Data.setFighting(picked2Data);
    picked2Data.setFighting(picked1Data);
    fighting.add(picked1);
    fighting.add(picked2);

    PlayerUtil.clearPlayer(picked1, false);
    PlayerUtil.clearPlayer(picked2, false);
    picked1.teleport(getSumoLocations()[0].toBukkitLocation());
    picked2.teleport(getSumoLocations()[1].toBukkitLocation());

    for (Player other : getBukkitPlayers()) {
      if (other != null) {
        PlayerUtil.hideOrShowPlayer(other, picked1, false);
        PlayerUtil.hideOrShowPlayer(other, picked2, false);
      }
    }

    for (UUID spectatorUUID : getPlugin().getManagerHandler().getEventManager().getSpectators()
        .keySet()) {
      Player spectator = Bukkit.getPlayer(spectatorUUID);
      if (spectator != null) {
        PlayerUtil.hideOrShowPlayer(spectator, picked1, false);
        PlayerUtil.hideOrShowPlayer(spectator, picked2, false);
      }
    }

    PlayerUtil.hideOrShowPlayer(picked2, picked1, false);
    PlayerUtil.hideOrShowPlayer(picked1, picked2, false);

    round++;

    sendMessage(
        getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ROUND-STARTING")
            .replace("<round>", String.valueOf(round))
            .replace("<playerA>", picked1.getName())
            .replace("<playerB>", picked2.getName())
    );

    BukkitTask task = new SumoFightTask(picked1, picked2, picked1Data, picked2Data).runTaskTimer(
        getPlugin(), 0, 20);
    picked1Data.setFightTask(task);
    picked2Data.setFightTask(task);
    playerA = picked1;
    playerB = picked2;

    if (getPlugin().isUsingCustomKB()) {
      getKitOptional().ifPresent(kit -> {
        PlayerManager.setKnockbackProfile(playerA, kit.getKbProfile());
        PlayerManager.setKnockbackProfile(playerB, kit.getKbProfile());
      });
    }
  }

  private Player getRandomPlayer() {
    if (getByState(SumoPlayer.SumoState.WAITING).size() == 0) {
      return null;
    }

    List<UUID> waiting = getByState(SumoPlayer.SumoState.WAITING);
    Collections.shuffle(waiting);
    UUID uuid = waiting.get(ThreadLocalRandom.current().nextInt(waiting.size()));

    getPlayer(uuid).setState(SumoPlayer.SumoState.PREPARING);

    return getPlugin().getServer().getPlayer(uuid);
  }

  public List<UUID> getByState(SumoPlayer.SumoState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(SumoPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class SumoFightTask extends BukkitRunnable {

    private final Player player;
    private final Player other;

    private final SumoPlayer playerSumo;
    private final SumoPlayer otherSumo;

    private int time = 90;

    @Override
    public void run() {
      if (player == null || other == null || !player.isOnline() || !other.isOnline()) {
        cancel();
        return;
      }

      if (time == 90) {
        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.ROUND-STARTING-THREE"));
      } else if (time == 89) {
        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.ROUND-STARTING-TWO"));
      } else if (time == 88) {
        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.ROUND-STARTING-ONE"));
      } else if (time == 87) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ROUND-STARTED"));
        otherSumo.setState(SumoPlayer.SumoState.FIGHTING);
        playerSumo.setState(SumoPlayer.SumoState.FIGHTING);
      } else if (time <= 0) {
        List<Player> players = Arrays.asList(player, other);
        Player winner = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        players.stream().filter(pl -> !pl.equals(winner)).forEach(pl -> onDeath().accept(pl));

        cancel();
        return;
      }

      if (Arrays.asList(30, 25, 20, 15, 10, 5, 4, 3, 2, 1).contains(time)) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ROUND-ENDING")
                .replace("<countdown>", String.valueOf(time)));
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
            && getPlayer(player).getState() != SumoPlayer.SumoState.FIGHTING) {
          return;
        }
        if (PlayerUtil.isStandingOnLiquid(player)) {
          onDeath().accept(player);
        }
      });
    }
  }
}