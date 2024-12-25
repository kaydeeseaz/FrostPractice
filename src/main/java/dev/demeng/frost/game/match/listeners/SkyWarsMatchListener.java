package dev.demeng.frost.game.match.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.managers.chest.event.ChestBreakEvent;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SkyWarsMatchListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler
  public void onGameChestBreak(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (player == null) {
      return;
    }

    PracticePlayerData data = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (data.isInEvent() && plugin.getManagerHandler().getEventManager()
        .getOngoingEvent() instanceof SkyWarsEvent || data.isInMatch() && plugin.getManagerHandler()
        .getMatchManager().getMatch(data).getKit().isSkyWars()) {
      if (player.getGameMode() != GameMode.SURVIVAL) {
        return;
      }

      Block block = event.getClickedBlock();
      if (block == null) {
        return;
      }
      if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
        Chest chest = (Chest) block.getState();
        Inventory inv = chest.getInventory();
        ItemStack[] toDrop;

        if (PlayerUtil.isInventoryEmpty(inv)) {
          toDrop = plugin.getManagerHandler().getChestManager().getRandomItemsFromChests();
        } else {
          toDrop = inv.getContents();
        }

        if (toDrop != null) {
          block.setType(Material.AIR);

          Set<Item> items = new HashSet<>();
          Bukkit.getServer().getPluginManager()
              .callEvent(new ChestBreakEvent(player, inv, chest.getLocation()));
          for (ItemStack item : toDrop) {
            if (item != null && !item.getType().equals(Material.AIR)) {
              if (data.isInMatch()) {
                items.add(
                    player.getWorld().dropItemNaturally(block.getLocation().add(0, 1.5, 0), item));
                plugin.getManagerHandler().getMatchManager()
                    .addDroppedItems(plugin.getManagerHandler().getMatchManager().getMatch(data),
                        items);
              } else if (data.isInEvent()) {
                player.getInventory().addItem(item);
              }
            }
          }

          sendOpenChestParticles(player);
        }
      }
    }
  }

  private void sendOpenChestParticles(Player player) {
    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, false,
        (float) player.getLocation().getX(), (float) player.getLocation().getY(),
        (float) player.getLocation().getZ(), 0.2f, 0.2f, 0.2f, 1.0f, 20);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 0.5f);
  }
}
