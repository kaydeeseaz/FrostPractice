package dev.demeng.frost.game.ffa;

import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.managers.PlayerManager;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

@Getter
@Setter
public class FfaInstance {

  private final Frost plugin;
  private final Kit kit;
  private final Map<Item, Long> ffaItems = new HashMap<>();
  private final Map<UUID, Integer> killStreakTracker = new HashMap<>();
  private final Set<UUID> ffaPlayers = new HashSet<>();

  public FfaInstance(Frost plugin, Kit kit) {
    this.plugin = plugin;
    this.kit = kit;
  }

  public void addPlayer(Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    practicePlayerData.setPlayerState(PlayerState.FFA);
    player.setAllowFlight(false);
    player.setFlying(false);

    getFfaPlayers().add(player.getUniqueId());
    getKillStreakTracker().put(player.getUniqueId(), 0);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      getKit().applyKit(player);
      player.teleport(
          plugin.getManagerHandler().getSpawnManager().getFfaLocation().toBukkitLocation());
    }, 1L);

    for (String lines : plugin.getMessagesConfig().getConfig()
        .getStringList("MESSAGES.FFA.JOINED")) {
      sendMessage(player, lines.replace("<players>", String.valueOf(getFfaPlayers().size()))
          .replace("<kit>", getKit().getName()));
    }

    player.getActivePotionEffects()
        .forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
    PlayerManager.setKnockbackProfile(player, getKit().getKbProfile());

    for (PracticePlayerData playerData : plugin.getManagerHandler().getPlayerManager()
        .getAllData()) {
      Player ffaPlayer = plugin.getServer().getPlayer(playerData.getUniqueId());
      if (playerData.getPlayerState() == PlayerState.FFA && getFfaPlayers().contains(
          playerData.getUniqueId())) {
        PlayerUtil.hideOrShowPlayer(ffaPlayer, player, false);
        PlayerUtil.hideOrShowPlayer(player, ffaPlayer, false);
      } else {
        PlayerUtil.hideOrShowPlayer(ffaPlayer, player, true);
        PlayerUtil.hideOrShowPlayer(player, ffaPlayer, true);
      }
    }
  }

  public void removePlayer(Player player) {
    for (PracticePlayerData playerData : plugin.getManagerHandler().getPlayerManager()
        .getAllData()) {
      Player ffaPlayer = plugin.getServer().getPlayer(playerData.getUniqueId());
      if (playerData.getPlayerState() == PlayerState.FFA) {
        PlayerUtil.hideOrShowPlayer(ffaPlayer, player, true);
        PlayerUtil.hideOrShowPlayer(player, ffaPlayer, true);
      }
    }

    getFfaPlayers().remove(player.getUniqueId());
    getKillStreakTracker().remove(player.getUniqueId());
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);

    for (String s : plugin.getMessagesConfig().getConfig().getStringList("MESSAGES.FFA.LEFT")) {
      sendMessage(player, s);
    }
  }
}
