package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public abstract class DuplicateArenaRunnable extends BukkitRunnable {

  private final Frost plugin;
  private final Arena copiedArena;
  private int offsetX;
  private int offsetZ;
  private final int incrementX;
  private final int incrementZ;
  private Map<Location, Block> paste;

  public DuplicateArenaRunnable(Frost plugin, Arena copiedArena, int offsetX, int offsetZ,
      int incrementX, int incrementZ) {
    this.plugin = plugin;
    this.copiedArena = copiedArena;
    this.offsetX = offsetX;
    this.offsetZ = offsetZ;
    this.incrementX = incrementX;
    this.incrementZ = incrementZ;
  }

  @Override
  public void run() {
    if (this.paste == null) {
      Map<Location, Block> copy = this.blocksFromTwoPoints(
          this.copiedArena.getMin().toBukkitLocation(),
          this.copiedArena.getMax().toBukkitLocation());
      this.paste = new HashMap<>();
      for (Map.Entry<Location, Block> entry : copy.entrySet()) {
        if (entry.getValue().getType() != Material.AIR) {
          this.paste.put(entry.getKey().clone().add(this.offsetX, 0, this.offsetZ),
              copy.get(entry.getKey()));
        }
      }
      copy.clear();
    } else {
      Map<Location, Block> newPaste = new HashMap<>();
      for (Map.Entry<Location, Block> entry : this.paste.entrySet()) {
        if (entry.getValue().getType() != Material.AIR) {
          newPaste.put(entry.getKey().clone().add(this.incrementX, 0, this.incrementZ),
              this.paste.get(entry.getKey()));
        }
      }

      this.paste.clear();
      this.paste.putAll(newPaste);
    }

    boolean safe = true;
    for (Location loc : this.paste.keySet()) {
      Block block = loc.getBlock();
      if (block.getType() != Material.AIR) {
        safe = false;
        break;
      }
    }

    if (!safe) {
      this.offsetX += this.incrementX;
      this.offsetZ += this.incrementZ;
      this.run();
      return;
    }

    new BlockPlaceRunnable(this.copiedArena.getA().toBukkitLocation().getWorld(), this.paste) {
      @Override
      public void finish() {
        DuplicateArenaRunnable.this.onComplete();
      }
    }.runTaskTimer(plugin, 0L, 5L);
  }

  public Map<Location, Block> blocksFromTwoPoints(Location loc1, Location loc2) {
    Map<Location, Block> blocks = new HashMap<>();

    int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
    int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());

    int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
    int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());

    int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
    int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());

    for (int x = bottomBlockX; x <= topBlockX; x++) {
      for (int z = bottomBlockZ; z <= topBlockZ; z++) {
        for (int y = bottomBlockY; y <= topBlockY; y++) {
          Block block = loc1.getWorld().getBlockAt(x, y, z);
          if (block.getType() != Material.AIR) {
            blocks.put(new Location(loc1.getWorld(), x, y, z), block);
          }
        }
      }
    }

    return blocks;
  }

  public abstract void onComplete();
}
