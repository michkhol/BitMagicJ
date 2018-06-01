package io.bitjynx;

abstract class AbstractMapBlock implements IBlock{
  final static int CELL_POWER = 6;
  protected  final static long HIGH_MASK = 0x8000000000000000L;
  protected final static int CELL_BIT_MASK = (1 << CELL_POWER) - 1;

  public static native void intersect0(long[] src1, long[] src2, long[] dst);

  // The size is always BLOCK_SIZE / sizeof(long)
  final protected long[] _map;

  AbstractMapBlock(long[] map) { _map = map; }
}
