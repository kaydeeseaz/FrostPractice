package dev.demeng.frost.managers.chunk.restoration.impl;

import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.managers.chunk.data.NekoChunk;
import dev.demeng.frost.managers.chunk.data.NekoChunkData;
import dev.demeng.frost.managers.chunk.reset.INekoChunkReset;
import dev.demeng.frost.managers.chunk.restoration.IChunkRestoration;
import dev.demeng.frost.util.cuboid.Cuboid;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.ChunkSection;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;

@RequiredArgsConstructor
public class FrostChunkRestoration implements IChunkRestoration {

  private final INekoChunkReset iNekoChunkReset;
  private final ChunkRestorationManager chunkRestorationManager;

  public void copy(StandaloneArena standaloneArena) {
    Cuboid cuboid = new Cuboid(standaloneArena.getMin().toBukkitLocation(),
        standaloneArena.getMax().toBukkitLocation());

    long startTime = System.currentTimeMillis();

    NekoChunkData nekoChunkData = new NekoChunkData();
    cuboid.getChunks().forEach(chunk -> {
      chunk.load();
      net.minecraft.server.v1_8_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
      ChunkSection[] nmsSections = iNekoChunkReset.cloneSections(nmsChunk.getSections());
      nekoChunkData.chunks.put(new NekoChunk(chunk.getX(), chunk.getZ()),
          iNekoChunkReset.cloneSections(nmsSections));
    });
    chunkRestorationManager.getChunks().put(standaloneArena, nekoChunkData);

    System.out.println("Chunks copied! (" + (System.currentTimeMillis() - startTime) + "ms)");
  }

  public void reset(StandaloneArena standaloneArena) {
    long startTime = System.currentTimeMillis();

    Cuboid cuboid = new Cuboid(standaloneArena.getMin().toBukkitLocation(),
        standaloneArena.getMax().toBukkitLocation());
    resetCuboid(cuboid, chunkRestorationManager.getChunks().get(standaloneArena));

    System.out.println(
        "Chunks have been reset! (took " + (System.currentTimeMillis() - startTime) + "ms)");
  }

  public void copy(Cuboid cuboid) {
    long startTime = System.currentTimeMillis();

    NekoChunkData nekoChunkData = new NekoChunkData();
    cuboid.getChunks().forEach(chunk -> {
      chunk.load();
      net.minecraft.server.v1_8_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
      ChunkSection[] nmsSections = iNekoChunkReset.cloneSections(nmsChunk.getSections());
      nekoChunkData.chunks.put(new NekoChunk(chunk.getX(), chunk.getZ()),
          iNekoChunkReset.cloneSections(nmsSections));
    });
    chunkRestorationManager.getEventMapChunks().put(cuboid, nekoChunkData);

    System.out.println(
        "Chunks copied for SkyWars Event! (" + (System.currentTimeMillis() - startTime) + "ms)");
  }

  public void reset(Cuboid cuboid) {
    long startTime = System.currentTimeMillis();
    resetCuboid(cuboid, chunkRestorationManager.getEventMapChunks().get(cuboid));

    System.out.println(
        "Chunks have been reset for SkyWars Event! (took " + (System.currentTimeMillis()
            - startTime) + "ms)");
  }

  private void resetCuboid(Cuboid cuboid, NekoChunkData nekoChunkData) {
    cuboid.getChunks().forEach(chunk -> {
      try {
        chunk.load();
        iNekoChunkReset.setSections(((CraftChunk) chunk).getHandle(),
            iNekoChunkReset.cloneSections(nekoChunkData.getNyaChunk(chunk.getX(), chunk.getZ())));
        chunk.getWorld().refreshChunk(chunk.getX(),
            chunk.getZ()); // let the mf server know that you've updated the chunk.
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}