package dev.demeng.frost.managers.chunk.restoration;

import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.util.cuboid.Cuboid;

public interface IChunkRestoration {

  void copy(StandaloneArena standaloneArena);

  void reset(StandaloneArena standaloneArena);

  void copy(Cuboid cuboid);

  void reset(Cuboid cuboid);
}