package io.bitjynx;

import java.util.Map;
import java.util.concurrent.RecursiveTask;

interface IBlock {
  public BlockType getType();

  public long size();
  public int cardinality();

  /**
   * Checks if a bit set at specified position
   *
   * @param pos integer to cover the 'unsigned short' range
   * @return true if bit is set
   */
  public boolean exists(int pos);

  public int lastBitPos();

  public RecursiveTask<Map.Entry<Long, IBlock>> andTask(Long key, IBlock right);
  public RecursiveTask<Map.Entry<Long, IBlock>> orTask(Long key, IBlock right);
}
