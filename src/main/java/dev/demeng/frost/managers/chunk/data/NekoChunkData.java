package dev.demeng.frost.managers.chunk.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.ChunkSection;

@Getter
@Setter
public class NekoChunkData {

  public Map<NekoChunk, ChunkSection[]> chunks = new ConcurrentHashMap<>();

  public ChunkSection[] getNyaChunk(int x, int z) {
    for (Map.Entry<NekoChunk, ChunkSection[]> chunksFromMap : chunks.entrySet()) {
      if (chunksFromMap.getKey().getX() == x && chunksFromMap.getKey().getZ() == z) {
        return chunksFromMap.getValue();
      }
    }

    return null;
  }
}
