package dev.demeng.frost.runnable;

import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.CuboidRegion;
import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaCopyRemovalRunnable extends BukkitRunnable {

  private final int number;
  private final Arena arena;
  private final StandaloneArena arenaCopy;

  private final Frost plugin = Frost.getInstance();

  public ArenaCopyRemovalRunnable(int number, Arena arena, StandaloneArena arenaCopy) {
    this.number = number;
    this.arena = arena;
    this.arenaCopy = arenaCopy;
  }

  @Override
  public void run() {
    TaskManager.IMP.async(() -> {
      EditSession editSession = new EditSessionBuilder(arenaCopy.getA().getWorld()).fastmode(true)
          .allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
      CuboidRegion copyRegion = new CuboidRegion(
          new Vector(arenaCopy.getMax().getX(), arenaCopy.getMax().getY(),
              arenaCopy.getMax().getZ()),
          new Vector(arenaCopy.getMin().getX(), arenaCopy.getMin().getY(),
              arenaCopy.getMin().getZ())
      );

      try {
        editSession.setBlocks(copyRegion, new BaseBlock(BlockID.AIR));
      } catch (MaxChangedBlocksException e) {
        e.getStackTrace();
      }

      editSession.flushQueue();
    });

    plugin.getArenasConfig().getConfig()
        .getConfigurationSection("arenas." + arena.getName() + ".standaloneArenas")
        .set(String.valueOf(number), null);
    plugin.getArenasConfig().save();

    plugin.getManagerHandler().getArenaManager().getArena(arena.getName()).getStandaloneArenas()
        .remove(arenaCopy);
    plugin.getManagerHandler().getArenaManager().getArena(arena.getName()).getAvailableArenas()
        .remove(number);
  }
}
