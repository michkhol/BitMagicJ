package io.bitjynx;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BitJynx {
  final static int BIT_BLOCK_POWER = 16;
  final static int BITS_PER_BLOCK = 1 << BIT_BLOCK_POWER;

  // Taken from java.util.Arrays, adapted for 'unsigned short'
  private static int unsignedBinarySearch(short[] a, int key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      short midVal = a[mid];

      int intMidVal = Short.toUnsignedInt(midVal);
      if (intMidVal < key)
        low = mid + 1;
      else if (intMidVal > key)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1);  // key not found.
  }

  static class Block implements IBlock {

    // Used as 'unsigned short'
    private short[] _positions;

    Block(short[] positions) {
      this._positions = positions;
    }

    @Override
    public long size() { return _positions.length; }

    @Override
    public int cardinality() { return _positions.length; }

    @Override
    public boolean exists(int pos) {
      int result = unsignedBinarySearch(_positions, pos);
      return result >= 0;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Block size ").append(_positions.length).append(": [ ");
      boolean first = true;
      for(short i: _positions) {
        int v = Short.toUnsignedInt(i); // simulating unsigned int for display
        if (first) {
          first = false;
          sb.append(v);
        } else
          sb.append(", ").append(v);
      }
      sb.append(" ]");
      return sb.toString();
    }
  }

  static class FullBlock implements IBlock {

    @Override
    public long size() { return BITS_PER_BLOCK; }

    @Override
    public int cardinality() { return BITS_PER_BLOCK; }

    @Override
    public boolean exists(int pos) { return true; }

    @Override
    public String toString() {
      return "Block size " + BITS_PER_BLOCK + ": [ all set ]";
    }
  }

  private static FullBlock _fullBlock = new FullBlock();

  private LinkedHashMap<Long, IBlock> _blockMap;
  private long _totalBits;
  private long _maxBit;

  private BitJynx(LinkedHashMap<Long, IBlock> blocks, long totalBits, long maxBit) {
    this._blockMap = blocks;
    this._totalBits = totalBits;
    this._maxBit = maxBit;
  }

  public BitJynx(long[] bits) {
    // sort it
    Arrays.parallelSort(bits);
    _totalBits = bits.length;
    _maxBit = bits[(int)_totalBits - 1];
    _blockMap = new LinkedHashMap<>();
    storeData(bits);
  }

  /**
   *
   * @param bits - must be sorted!!!
   */
  private void storeData(long[] bits) {
    short[] tempBlock = new short[BITS_PER_BLOCK];
    long blockStart = 0;
    int bitIdx = 0;
    for(long bitNo: bits) {
      if(bitNo >= blockStart + BITS_PER_BLOCK) {
        if (bitIdx > 0) {
          // Store previous block first
          short[] block = new short[bitIdx];
          System.arraycopy(tempBlock, 0, block, 0, bitIdx);
          long blockIdx = blockStart >> BIT_BLOCK_POWER;
          _blockMap.put(blockIdx, bitIdx == BITS_PER_BLOCK ? _fullBlock : new Block(block));
          bitIdx = 0;
        }
        blockStart += BITS_PER_BLOCK;
      }
      short pos = (short)(bitNo - blockStart); // the cast effectively makes 'unsigned short'
      tempBlock[bitIdx++] = pos;
    }
    if (bitIdx > 0) {
      // Store the rest
      short[] block = new short[bitIdx];
      System.arraycopy(tempBlock, 0, block, 0, bitIdx);
      long blockIdx = blockStart >> BIT_BLOCK_POWER;
      _blockMap.put(blockIdx, bitIdx == BITS_PER_BLOCK ? _fullBlock : new Block(block));
    }

  }

  public long cardinality() { return _totalBits; }

  public boolean get(long idx) {
    // find the block
    long blockIdx = idx >> BIT_BLOCK_POWER;
    IBlock b = _blockMap.get(blockIdx);
    if (b == null) return false;
    else {
      int pos = (int)(idx - (blockIdx << BIT_BLOCK_POWER));
      return b.exists(pos);
    }
  }

  public int set(long idx, boolean v) {
    long blockIdx = idx >> BIT_BLOCK_POWER;
    IBlock b = _blockMap.get(blockIdx);
    return 0;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(_totalBits);
    sb.append(", highest bit: ").append(_maxBit);
    if (_blockMap != null) {
      final AtomicLong counter = new AtomicLong();
      sb.append(", blocks: ").append(_blockMap.size()).append("\n");
      _blockMap.forEach((k, v) -> { sb.append(k).append(": ").append(v.toString()).append("\n"); counter.addAndGet(v.size()); });
      sb.append("Total block size: ").append(counter.get());
    }
    return sb.toString();
  }
}
