package dev.demeng.frost.managers.chunk;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.util.CustomLocation;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class ChunkManager {

  private final Frost plugin = Frost.getInstance();
  private boolean chunksLoaded;

  public ChunkManager() {
    new BukkitRunnable() {
      @Override
      public void run() {
        loadChunks();
      }
    }.runTaskLater(plugin, 2L);
  }

  private void loadChunks() {
    plugin.getLogger().info("Starting chunk loading task!");

    CustomLocation spawnMin = plugin.getManagerHandler().getSpawnManager().getSpawnMin();
    CustomLocation spawnMax = plugin.getManagerHandler().getSpawnManager().getSpawnMax();

    if (spawnMin != null && spawnMax != null) {
      int spawnMinX = spawnMin.toBukkitLocation().getBlockX() >> 4;
      int spawnMinZ = spawnMin.toBukkitLocation().getBlockZ() >> 4;
      int spawnMaxX = spawnMax.toBukkitLocation().getBlockX() >> 4;
      int spawnMaxZ = spawnMax.toBukkitLocation().getBlockZ() >> 4;

      if (spawnMinX > spawnMaxX) {
        int lastSpawnMinX = spawnMinX;
        spawnMinX = spawnMaxX;
        spawnMaxX = lastSpawnMinX;
      }

      if (spawnMinZ > spawnMaxZ) {
        int lastSpawnMinZ = spawnMinZ;
        spawnMinZ = spawnMaxZ;
        spawnMaxZ = lastSpawnMinZ;
      }

      World spawnWorld = spawnMin.toBukkitWorld();
      for (int x = spawnMinX; x <= spawnMaxX; x++) {
        for (int z = spawnMinZ; z <= spawnMaxZ; z++) {
          Chunk chunk = spawnWorld.getChunkAt(x >> 4, z >> 4);
          if (!chunk.isLoaded()) {
            chunk.load();
          }
        }
      }
    } else {
      plugin.getLogger().info(" ");
      plugin.getLogger().info("                		WARNING");
      plugin.getLogger().info("Please make sure you set the Spawn Min & Max Locations!");
      plugin.getLogger().info("If you did not, remove 'spawnLocation' from settings.yml");
      plugin.getLogger().info("                		WARNING");
      plugin.getLogger().info(" ");
    }

    CustomLocation skywarsMin = plugin.getManagerHandler().getSpawnManager().getSkywarsMin();
    CustomLocation skywarsMax = plugin.getManagerHandler().getSpawnManager().getSkywarsMax();
    if (skywarsMin != null && skywarsMax != null) {
      int skywarsMinX = skywarsMin.toBukkitLocation().getBlockX() >> 4;
      int skywarsMinZ = skywarsMin.toBukkitLocation().getBlockZ() >> 4;
      int skywarsMaxX = skywarsMin.toBukkitLocation().getBlockX() >> 4;
      int skywarsMaxZ = skywarsMin.toBukkitLocation().getBlockZ() >> 4;

      if (skywarsMinX > skywarsMaxX) {
        int lastskywarsMinX = skywarsMinX;
        skywarsMinX = skywarsMaxX;
        skywarsMaxX = lastskywarsMinX;
      }

      if (skywarsMinZ > skywarsMaxZ) {
        int lastskywarsMaxZ = skywarsMinZ;
        skywarsMinZ = skywarsMaxZ;
        skywarsMaxZ = lastskywarsMaxZ;
      }

      World skyWarsWorld = skywarsMin.toBukkitWorld();
      for (int x = skywarsMinX; x <= skywarsMaxX; x++) {
        for (int z = skywarsMinZ; z <= skywarsMaxZ; z++) {
          Chunk chunk = skyWarsWorld.getChunkAt(x >> 4, z >> 4);
          if (!chunk.isLoaded()) {
            chunk.load();
          }
        }
      }
    }

    for (Arena arena : plugin.getManagerHandler().getArenaManager().getArenas().values()) {
      if (!arena.isEnabled()) {
        continue;
      }

      int arenaMinX = arena.getMin().toBukkitLocation().getBlockX() >> 4;
      int arenaMinZ = arena.getMin().toBukkitLocation().getBlockZ() >> 4;
      int arenaMaxX = arena.getMax().toBukkitLocation().getBlockX() >> 4;
      int arenaMaxZ = arena.getMax().toBukkitLocation().getBlockZ() >> 4;

      if (arenaMinX > arenaMaxX) {
        int lastArenaMinX = arenaMinX;
        arenaMinX = arenaMaxX;
        arenaMaxX = lastArenaMinX;
      }

      if (arenaMinZ > arenaMaxZ) {
        int lastArenaMinZ = arenaMinZ;
        arenaMinZ = arenaMaxZ;
        arenaMaxZ = lastArenaMinZ;
      }

      World arenaWorld = arena.getMin().toBukkitWorld();
      for (int x = arenaMinX; x <= arenaMaxX; x++) {
        for (int z = arenaMinZ; z <= arenaMaxZ; z++) {
          Chunk chunk = arenaWorld.getChunkAt(x >> 4, z >> 4);
          if (!chunk.isLoaded()) {
            chunk.load();
          }
        }
      }

      for (StandaloneArena saArena : arena.getStandaloneArenas()) {
        arenaMinX = saArena.getMin().toBukkitLocation().getBlockX() >> 4;
        arenaMinZ = saArena.getMin().toBukkitLocation().getBlockZ() >> 4;
        arenaMaxX = saArena.getMax().toBukkitLocation().getBlockX() >> 4;
        arenaMaxZ = saArena.getMax().toBukkitLocation().getBlockZ() >> 4;

        if (arenaMinX > arenaMaxX) {
          int lastArenaMinX = arenaMinX;
          arenaMinX = arenaMaxX;
          arenaMaxX = lastArenaMinX;
        }

        if (arenaMinZ > arenaMaxZ) {
          int lastArenaMinZ = arenaMinZ;
          arenaMinZ = arenaMaxZ;
          arenaMaxZ = lastArenaMinZ;
        }

        World standaloneArenaWorld = saArena.getMin().toBukkitWorld();
        for (int x = arenaMinX; x <= arenaMaxX; x++) {
          for (int z = arenaMinZ; z <= arenaMaxZ; z++) {
            Chunk chunk = standaloneArenaWorld.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) {
              chunk.load();
            }
          }
        }
      }
    }

    plugin.getLogger().info("Finished loading all the chunks!");
    this.chunksLoaded = true;
  }

  public boolean areChunksLoaded() {
    return chunksLoaded;
  }
}
