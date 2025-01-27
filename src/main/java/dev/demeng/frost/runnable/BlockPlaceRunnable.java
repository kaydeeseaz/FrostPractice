package dev.demeng.frost.runnable;

import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public abstract class BlockPlaceRunnable extends BukkitRunnable {

  private final ConcurrentMap<Location, Block> blocks;
  private final int totalBlocks;
  private final Iterator<Location> iterator;
  private final World world;
  private boolean completed = false;

  protected BlockPlaceRunnable(World world, Map<Location, Block> blocks) {
    this.world = world;
    this.blocks = new ConcurrentHashMap<>();
    this.blocks.putAll(blocks);
    this.totalBlocks = blocks.keySet().size();
    this.iterator = blocks.keySet().iterator();
  }

  @Override
  public void run() {
    if (blocks.isEmpty() || !iterator.hasNext()) {
      finish();
      completed = true;
      cancel();
      return;
    }

    TaskManager.IMP.async(() -> {
      EditSession editSession = new EditSessionBuilder(this.world.getName()).fastmode(true)
          .allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
      for (Map.Entry<Location, Block> entry : this.blocks.entrySet()) {
        try {
          editSession.setBlock(new Vector(entry.getKey().getBlockX(), entry.getKey().getBlockY(),
                  entry.getKey().getZ()),
              new BaseBlock(entry.getValue().getTypeId(), entry.getValue().getData()));
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      editSession.flushQueue();
      TaskManager.IMP.task(this.blocks::clear);
    });
  }

  public abstract void finish();
}
