package io.bitjynx;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

import static io.bitjynx.BitJynx.BITS_PER_BLOCK;

class FullBlock implements IBlock {

  public final static FullBlock instance = new FullBlock();

  @Override
  public BlockType getType() { return BlockType.FULL_BLOCK; }

  @Override
  public long size() { return BITS_PER_BLOCK; }

  @Override
  public int cardinality() { return BITS_PER_BLOCK; }

  @Override
  public boolean exists(int pos) { return true; }

  @Override
  public int lastBitPos() { return BITS_PER_BLOCK - 1; }

  @Override
  public RecursiveTask<Map.Entry<Long, IBlock>> andTask(Long key, IBlock right) {
    return new RecursiveTask<Map.Entry<Long, IBlock>>() {
      @Override
      protected Map.Entry<Long, IBlock> compute() {
        return new AbstractMap.SimpleEntry<>(key, right);
      }
    };
  }

  @Override
  public RecursiveTask<Map.Entry<Long, IBlock>> orTask(Long key, IBlock right) {
    return new RecursiveTask<Map.Entry<Long, IBlock>>() {
      @Override
      protected Map.Entry<Long, IBlock> compute() {
        return new AbstractMap.SimpleEntry<>(key, instance);
      }
    };
  }

  @Override
  public String toString() {
    return "BitPosBlock size " + BITS_PER_BLOCK + ": [ all set ]";
  }
}
