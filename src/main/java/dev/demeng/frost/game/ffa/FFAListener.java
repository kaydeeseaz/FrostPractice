package dev.demeng.frost.game.ffa;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class FFAListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity().getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    if (practicePlayerData.getPlayerState() == PlayerState.FFA) {
      FfaInstance ffaInstance = plugin.getManagerHandler().getFfaManager().getByPlayer(player);

      for (ItemStack item : player.getInventory().getContents()) {
        if (item != null && (item.getType() == Material.MUSHROOM_SOUP
            || item.getType() == Material.POTION)) {
          ffaInstance.getFfaItems()
              .put(player.getWorld().dropItemNaturally(player.getLocation(), item),
                  System.currentTimeMillis());
        }
      }

      ffaInstance.getKillStreakTracker().put(player.getUniqueId(), 0);
      Player killer = player.getKiller();
      if (killer != null) {
        ffaInstance.getKillStreakTracker().put(killer.getUniqueId(),
            ffaInstance.getKillStreakTracker().get(killer.getUniqueId()) + 1);
      }

      for (UUID ffaPlayer : ffaInstance.getFfaPlayers()) {
        CC.sendMessage(Bukkit.getPlayer(ffaPlayer),
            plugin.getMessagesConfig().getConfig().getString("MESSAGES.FFA.PLAYER-KILLED")
                .replace("<killed>", player.getName())
                .replace("<killer>", killer != null ? killer.getName() : " the enviroment.")
        );
      }

      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Frost.getInstance(),
          () -> ((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(
              PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN)), 1L);
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() == PlayerState.FFA) {
      FfaInstance ffaInstance = plugin.getManagerHandler().getFfaManager().getByPlayer(player);

      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        ffaInstance.getKit().applyKit(player);
        player.teleport(
            plugin.getManagerHandler().getSpawnManager().getFfaLocation().toBukkitLocation());
      }, 1L);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player) {
      Player player = (Player) event.getEntity();
      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (practicePlayerData == null) {
        return;
      }

      if (practicePlayerData.getPlayerState() == PlayerState.FFA) {
        event.setCancelled(player.getLocation().distance(
            plugin.getManagerHandler().getSpawnManager().getFfaLocation().toBukkitLocation())
            < plugin.getSettingsConfig().getConfig()
            .getInt("SETTINGS.GENERAL.FFA-SAFEZONE-RADIUS"));
      }
    }
  }
}
