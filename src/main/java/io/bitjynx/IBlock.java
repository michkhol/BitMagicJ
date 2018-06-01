package io.bitjynx;

import java.util.stream.IntStream;

interface IBlock {
  enum Type { EMPTY_BLOCK, UNITY_BLOCK, POS_BLOCK, ZERO_POS_BLOCK, BIT_MAP_BLOCK, ZERO_MAP_BLOCK }

  public Type getType();

  public int cardinality();

  /**
   * Checks if a bit set at specified position
   *
   * @param pos integer to cover the 'unsigned short' range
   * @return true if bit is set
   */
  public boolean exists(int pos);

  public int lastBitPos();

  public IntStream stream();

  public IBlock not();

  public IBlock and(IBlock v2);
  public IBlock or(IBlock v2);
  public IBlock xor(IBlock v2);
  public IBlock nand(IBlock v2);
}
