package dev.demeng.frost.managers.chunk;

import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.managers.chunk.data.NekoChunkData;
import dev.demeng.frost.managers.chunk.reset.INekoChunkReset;
import dev.demeng.frost.managers.chunk.reset.impl.VanillaNekoChunkReset;
import dev.demeng.frost.managers.chunk.restoration.IChunkRestoration;
import dev.demeng.frost.managers.chunk.restoration.impl.FrostChunkRestoration;
import dev.demeng.frost.util.cuboid.Cuboid;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ChunkRestorationManager {

  @Getter @Setter(AccessLevel.PRIVATE) private static INekoChunkReset iNekoChunkReset;
  @Getter @Setter(AccessLevel.PRIVATE) private static IChunkRestoration iChunkRestoration;

  private final Map<StandaloneArena, NekoChunkData> chunks = new ConcurrentHashMap<>();
  private final Map<Cuboid, NekoChunkData> eventMapChunks = new ConcurrentHashMap<>();

  public ChunkRestorationManager() {
    if (iNekoChunkReset == null) {
      // Let the other plugins create an INekoReset before we load ours.
      setINekoChunkReset(new VanillaNekoChunkReset());
    }

    if (iChunkRestoration == null) {
      // Let the other plugins create an IChunkRestoration before we load ours.
      setIChunkRestoration(new FrostChunkRestoration(iNekoChunkReset, this));
    }
  }
}
