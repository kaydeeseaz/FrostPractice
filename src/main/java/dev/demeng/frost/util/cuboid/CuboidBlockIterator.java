package dev.demeng.frost.util.cuboid;

import java.util.Iterator;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CuboidBlockIterator implements Iterator<Block> {

  private final World world;
  private final int baseX;
  private final int baseY;
  private final int baseZ;
  private final int sizeX;
  private final int sizeY;
  private final int sizeZ;
  private int x;
  private int y;
  private int z;

  CuboidBlockIterator(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
    this.world = world;
    this.baseX = x1;
    this.baseY = y1;
    this.baseZ = z1;
    this.sizeX = Math.abs(x2 - x1) + 1;
    this.sizeY = Math.abs(y2 - y1) + 1;
    this.sizeZ = Math.abs(z2 - z1) + 1;
    this.z = 0;
    this.y = 0;
    this.x = 0;
  }

  @Override
  public boolean hasNext() {
    return this.x < this.sizeX && this.y < this.sizeY && this.z < this.sizeZ;
  }

  @Override
  public Block next() {
    Block block = this.world.getBlockAt(this.baseX + this.x, this.baseY + this.y,
        this.baseZ + this.z);
    if (++this.x >= this.sizeX) {
      this.x = 0;
      if (++this.y >= this.sizeY) {
        this.y = 0;
        ++this.z;
      }
    }
    return block;
  }

  @Override
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public World getWorld() {
    return this.world;
  }

  public int getBaseX() {
    return this.baseX;
  }

  public int getBaseY() {
    return this.baseY;
  }

  public int getBaseZ() {
    return this.baseZ;
  }

  public int getSizeX() {
    return this.sizeX;
  }

  public int getSizeY() {
    return this.sizeY;
  }

  public int getSizeZ() {
    return this.sizeZ;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getZ() {
    return this.z;
  }
}

