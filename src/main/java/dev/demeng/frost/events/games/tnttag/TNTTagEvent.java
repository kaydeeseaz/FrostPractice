package dev.demeng.frost.events.games.tnttag;

import dev.demeng.frost.events.EventCountdown;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.tasks.EventCountdownTask;
import dev.demeng.frost.util.CustomLocation;
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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTTagEvent extends PracticeEvent<TNTTagPlayer> {

  private final Map<UUID, TNTTagPlayer> players = new HashMap<>();
  private final EventCountdown eventCountdown = new EventCountdown(this, 2);

  @Getter @Setter private TNTTagTask tntTagTask;

  public TNTTagEvent() {
    super("TNTTag");
  }

  @Override
  public Map<UUID, TNTTagPlayer> getPlayers() {
    return players;
  }

  @Override
  public EventCountdownTask getCountdownTask() {
    return eventCountdown;
  }

  @Override
  public List<CustomLocation> getSpawnLocations() {
    return Collections.singletonList(
        this.getPlugin().getManagerHandler().getSpawnManager().getTntTagLocation());
  }

  @Override
  public void onStart() {
    new TNTTagGameTask().runTaskTimer(getPlugin(), 0, 20L);
  }

  @Override
  public Consumer<Player> onJoin() {
    return player -> players.put(player.getUniqueId(),
        new TNTTagPlayer(player.getUniqueId(), this));
  }

  @Override
  public Consumer<Player> onDeath() {
    return player -> {
      if (!player.isOnline()) {
        return;
      }

      TNTTagPlayer data = getPlayer(player);
      if (data.getState() == TNTTagPlayer.TNTTagState.TAGGED) {
        data.setState(TNTTagPlayer.TNTTagState.ELIMINATED);

        getPlayers().remove(player.getUniqueId());
        getPlugin().getManagerHandler().getEventManager().addSpectator(player,
            getPlugin().getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
            this);

        sendMessage("&8[&b&lTNT Tag Event&8] &c" + player.getName() + " &7has been eliminated!");

        if (this.getByState(TNTTagPlayer.TNTTagState.INGAME).size() <= 5) {
          this.getByState(TNTTagPlayer.TNTTagState.INGAME)
              .forEach(tntplayer -> new BukkitRunnable() {
                @Override
                public void run() {
                  Player other = Bukkit.getPlayer(tntplayer);
                  other.teleport(
                      getPlugin().getManagerHandler().getSpawnManager().getTntTagGameLocation()
                          .toBukkitLocation());
                }
              }.runTask(getPlugin()));

          this.getByState(TNTTagPlayer.TNTTagState.TAGGED)
              .forEach(tntplayer -> new BukkitRunnable() {
                @Override
                public void run() {
                  Player other = Bukkit.getPlayer(tntplayer);
                  other.teleport(
                      getPlugin().getManagerHandler().getSpawnManager().getTntTagGameLocation()
                          .toBukkitLocation());
                }
              }.runTask(getPlugin()));
        }
      }

      if (this.getByState(TNTTagPlayer.TNTTagState.INGAME).size() == 1
          && this.getByState(TNTTagPlayer.TNTTagState.TAGGED).size() == 0) {
        Player winner = Bukkit.getPlayer(this.getByState(TNTTagPlayer.TNTTagState.INGAME).get(0));
        if (winner != null) {
          handleWin(winner);
        }
        end();
      }
    };
  }

  public List<UUID> getByState(TNTTagPlayer.TNTTagState state) {
    return players.values().stream().filter(player -> player.getState() == state)
        .map(TNTTagPlayer::getUuid).collect(Collectors.toList());
  }

  public void explodePlayer() {
    getByState(TNTTagPlayer.TNTTagState.TAGGED).forEach(tntplayer -> {
      Player player = Bukkit.getPlayer(tntplayer);
      player.getLocation().getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
      onDeath().accept(player);
    });

    switch (getByState(TNTTagPlayer.TNTTagState.INGAME).size()) {
      case 30:
        for (int i = 1; i <= 6; i++) {
          pickRandomPlayer();
        }
        break;
      case 20:
        for (int i = 1; i <= 5; i++) {
          pickRandomPlayer();
        }
        break;
      case 15:
        for (int i = 1; i <= 4; i++) {
          pickRandomPlayer();
        }
        break;
      case 5:
        for (int i = 1; i <= 2; i++) {
          pickRandomPlayer();
        }
        break;
      default:
        pickRandomPlayer();
        break;
    }
  }

  public void tagPlayer(Player player, Player attacker) {
    if (getPlayer(player.getUniqueId()).getState() == TNTTagPlayer.TNTTagState.TAGGED) {
      return;
    }

    getPlayer(player.getUniqueId()).setState(TNTTagPlayer.TNTTagState.TAGGED);
    getPlayer(attacker.getUniqueId()).setState(TNTTagPlayer.TNTTagState.INGAME);

    player.getInventory().setHelmet(new ItemStack(Material.TNT, 1));
    player.getInventory().setItem(0, new ItemStack(Material.TNT, 1));

    for (PotionEffect effect : attacker.getActivePotionEffects()) {
      attacker.removePotionEffect(effect.getType());
    }
    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }

    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));

    player.getInventory().setHelmet(new ItemStack(Material.TNT, 1));
    attacker.getInventory().setHelmet(new ItemStack(Material.AIR, 1));

    attacker.getInventory().setItem(0, new ItemStack(Material.AIR, 1));
    player.getInventory().setItem(0, new ItemStack(Material.TNT, 1));

    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20f, 0.1f);

    sendMessage("&c" + player.getName() + " &7has the TNT!");
  }

  public void pickRandomPlayer() {
    if (getByState(TNTTagPlayer.TNTTagState.INGAME).size() == 0) {
      return;
    }

    List<UUID> waiting = getByState(TNTTagPlayer.TNTTagState.INGAME);
    Collections.shuffle(waiting);

    UUID uuid = waiting.get(ThreadLocalRandom.current().nextInt(waiting.size()));
    getPlayer(uuid).setState(TNTTagPlayer.TNTTagState.TAGGED);

    Player player = getPlugin().getServer().getPlayer(uuid);
    player.getInventory().setHelmet(new ItemStack(Material.TNT, 1));
    player.getInventory().setItem(0, new ItemStack(Material.TNT, 1));

    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }

    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20f, 0.1f);

    sendMessage("&c" + player.getName() + " &7has the TNT!");
  }

  @Getter
  @RequiredArgsConstructor
  public class TNTTagGameTask extends BukkitRunnable {

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

        for (TNTTagPlayer player : getPlayers().values()) {
          player.setState(TNTTagPlayer.TNTTagState.INGAME);
          this.cancel();
        }

        getBukkitPlayers().forEach(player -> {
          new BukkitRunnable() {
            @Override
            public void run() {
              player.teleport(
                  getPlugin().getManagerHandler().getSpawnManager().getTntTagGameLocation()
                      .toBukkitLocation());
            }
          }.runTask(getPlugin());

          for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
          }

          player.setGameMode(GameMode.ADVENTURE);
          player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        });

        switch (getByState(TNTTagPlayer.TNTTagState.INGAME).size()) {
          case 30:
            for (int i = 1; i <= 6; i++) {
              pickRandomPlayer();
            }
            break;
          case 20:
            for (int i = 1; i <= 5; i++) {
              pickRandomPlayer();
            }
            break;
          case 15:
            for (int i = 1; i <= 4; i++) {
              pickRandomPlayer();
            }
            break;
          case 5:
            for (int i = 1; i <= 2; i++) {
              pickRandomPlayer();
            }
            break;
          default:
            pickRandomPlayer();
            break;
        }

        tntTagTask = new TNTTagTask();
        tntTagTask.runTaskTimer(getPlugin(), 0L, 20L);

        return;
      }

      time--;
    }
  }

  @Getter
  @RequiredArgsConstructor
  public class TNTTagTask extends BukkitRunnable {

    private int time = 30;

    @Override
    public void run() {
      if (time <= 1) {
        explodePlayer();
        if (getByState(TNTTagPlayer.TNTTagState.INGAME).size() == 1
            && getByState(TNTTagPlayer.TNTTagState.TAGGED).size() == 0) {
          return;
        }

        time = 30;
      }

      time--;
    }
  }
}
