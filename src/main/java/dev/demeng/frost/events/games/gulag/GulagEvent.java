package dev.demeng.frost.events.games.gulag;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.managers.PlayerManager;
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
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GulagEvent extends PracticeEvent<GulagPlayer> {

  private final Map<UUID, GulagPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  @Getter final List<Player> fighting = new ArrayList<>();
  @Getter @Setter private int round;
  @Getter private Player playerA;
  @Getter private Player playerB;

  public GulagEvent() {
    super("Gulag");
  }

  @Override
  public Map<UUID, GulagPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        getPlugin().getManagerHandler().getSpawnManager().getGulagLocation());
  }

  @Override
  public void onStart() {
    round = 0;
    selectPlayers();
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(), new GulagPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      GulagPlayer data = getPlayer(player);
      if (data == null || data.getFighting() == null) {
        return;
      }

      if (data.getState() == GulagPlayer.GulagState.FIGHTING
          || data.getState() == GulagPlayer.GulagState.PREPARING) {
        GulagPlayer killerData = data.getFighting();
        Player killer = getPlugin().getServer().getPlayer(killerData.getUuid());

        data.getFightTask().cancel();
        killerData.getFightTask().cancel();

        data.setState(GulagPlayer.GulagState.ELIMINATED);
        killerData.setState(GulagPlayer.GulagState.WAITING);

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

        if (getByState(GulagPlayer.GulagState.WAITING).size() == 1) {
          Player winner = Bukkit.getPlayer(getByState(GulagPlayer.GulagState.WAITING).get(0));
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

  private CustomLocation[] getGulagLocations() {
    CustomLocation[] array = new CustomLocation[2];
    array[0] = getPlugin().getManagerHandler().getSpawnManager().getGulagFirst();
    array[1] = getPlugin().getManagerHandler().getSpawnManager().getGulagSecond();

    return array;
  }

  private void selectPlayers() {
    if (getByState(GulagPlayer.GulagState.WAITING).size() == 1) {
      Player winner = Bukkit.getPlayer(getByState(GulagPlayer.GulagState.WAITING).get(0));
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

    GulagPlayer picked1Data = getPlayer(picked1);
    GulagPlayer picked2Data = getPlayer(picked2);

    picked1Data.setFighting(picked2Data);
    picked2Data.setFighting(picked1Data);

    fighting.add(picked1);
    fighting.add(picked2);

    PlayerUtil.clearPlayer(picked1, false);
    PlayerUtil.clearPlayer(picked2, false);

    picked1.teleport(getGulagLocations()[0].toBukkitLocation());
    picked2.teleport(getGulagLocations()[1].toBukkitLocation());

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

    BukkitTask task = new GulagFightTask(picked1, picked2, picked1Data, picked2Data).runTaskTimer(
        getPlugin(), 0, 20);

    picked1Data.setFightTask(task);
    picked2Data.setFightTask(task);

    playerA = picked1;
    playerB = picked2;

    if (getPlugin().isUsingCustomKB()) {
      PlayerManager.setKnockbackProfile(playerA, "default");
      PlayerManager.setKnockbackProfile(playerB, "default");
    }

    ItemStack glock19 = new ItemStack(Material.IRON_HOE);
    ItemMeta glockMeta = glock19.getItemMeta();
    glockMeta.setDisplayName(CC.color("&8[&7Glock-19&8]"));
    glock19.setItemMeta(glockMeta);

    picked1.getInventory().addItem(glock19);
    picked2.getInventory().addItem(glock19);

    Firework fw = getPlugin().getManagerHandler().getSpawnManager().getGulagFirework()
        .toBukkitLocation().getWorld().spawn(
            getPlugin().getManagerHandler().getSpawnManager().getGulagFirework().toBukkitLocation(),
            Firework.class);
    FireworkMeta fwm = fw.getFireworkMeta();
    Builder builder = FireworkEffect.builder();

    fwm.addEffect(builder.flicker(true).withColor(Color.BLUE).build());
    fwm.addEffect(builder.trail(true).build());
    fwm.addEffect(builder.withFade(Color.RED).build());
    fwm.addEffect(builder.with(FireworkEffect.Type.BURST).build());
    fwm.setPower(2);
    fw.setFireworkMeta(fwm);
  }

  private Player getRandomPlayer() {
    if (getByState(GulagPlayer.GulagState.WAITING).size() == 0) {
      return null;
    }

    List<UUID> waiting = getByState(GulagPlayer.GulagState.WAITING);

    Collections.shuffle(waiting);

    UUID uuid = waiting.get(ThreadLocalRandom.current().nextInt(waiting.size()));

    getPlayer(uuid).setState(GulagPlayer.GulagState.PREPARING);

    return getPlugin().getServer().getPlayer(uuid);
  }

  public List<UUID> getByState(GulagPlayer.GulagState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(GulagPlayer::getUuid).collect(Collectors.toList());
  }

  @Getter
  @RequiredArgsConstructor
  public class GulagFightTask extends BukkitRunnable {

    private final Player player;
    private final Player other;

    private final GulagPlayer gulagPlayer;
    private final GulagPlayer gulagOpponent;

    private int time = 180;

    @Override
    public void run() {
      if (player == null || other == null || !player.isOnline() || !other.isOnline()) {
        cancel();
        return;
      }

      if (time == 180) {
        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.ROUND-STARTING-THREE"));
      } else if (time == 179) {
        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.ROUND-STARTING-TWO"));
      } else if (time == 178) {
        sendMessage(getPlugin().getMessagesConfig().getConfig()
            .getString("MESSAGES.EVENT.ROUND-STARTING-ONE"));
      } else if (time == 177) {
        sendMessage(
            getPlugin().getMessagesConfig().getConfig().getString("MESSAGES.EVENT.ROUND-STARTED"));
        gulagPlayer.setState(GulagPlayer.GulagState.FIGHTING);
        gulagOpponent.setState(GulagPlayer.GulagState.FIGHTING);
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
}
