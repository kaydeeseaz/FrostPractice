package dev.demeng.frost.game.match.listeners;

import dev.demeng.frost.Frost;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MatchFireballListener implements Listener {

  private final Frost plugin;

  public MatchFireballListener(Frost plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onBlockExplosion(BlockExplodeEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onBlockIgnition(BlockIgniteEvent event) {
    if (event.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onFireballHit(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player) {
      Player player = (Player) event.getEntity();
      if (event.getDamager() instanceof Fireball && plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId()).isInMatch()) {
        event.getEntity().setVelocity(event.getEntity().getVelocity().normalize().multiply(1));
        event.setDamage(0.0D);
      }
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
        .isInMatch()) {
      return;
    }

    ItemStack item = player.getInventory().getItemInHand();
    if (item.getType() == Material.FIREBALL) {
      if (item.getAmount() == 1) {
        player.getInventory().remove(item);
      } else {
        item.setAmount(item.getAmount() - 1);
      }

      Fireball fireball = player.launchProjectile(Fireball.class);
      fireball.setIsIncendiary(false);
      fireball.setVelocity(player.getLocation().getDirection().multiply(1));
      fireball.setYield(2.0F);
    }
  }

  @EventHandler
  public void onExplosion(EntityExplodeEvent event) {
    Location loc = event.getEntity().getLocation();
    if (event.getEntity() instanceof Fireball) {
      for (int x = loc.getBlockX() - 1; x <= loc.getBlockX() + 1; x++) {
        for (int y = loc.getBlockY() - 1; y <= loc.getBlockY() + 1; y++) {
          for (int z = loc.getBlockZ() - 1; z <= loc.getBlockZ() + 1; z++) {
            Block block = Objects.requireNonNull(loc.getWorld()).getBlockAt(x, y, z);
            if (block.getType() == Material.WOOL) {
              block.setType(Material.AIR);
            } else {
              event.setCancelled(true);
            }
          }
        }
      }

      Location location = event.getLocation();
      List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(location.getWorld())
          .getNearbyEntities(location, 5, 5, 5);
      for (Entity entity : nearbyEntities) {
        if (entity instanceof Player) {
          if (entity.getLocation().distance(event.getEntity().getLocation()) <= 5) {
            entity.setVelocity(
                entity.getLocation().subtract(event.getEntity().getLocation()).toVector().setY(0.5)
                    .normalize().multiply(1.25));
          }
        }
      }
    }
  }
}
