package io.bitjynx;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class BitJynx {
  final static int BIT_BLOCK_POWER = 13; // 8K blocks
  final static int BITS_PER_BLOCK = 1 << BIT_BLOCK_POWER;

  private static FullBlock _fullBlock = new FullBlock();

  private TreeMap<Long, IBlock> _blockMap;
  private long _totalBits;
  private long _maxBitPosition; // Do we really need this?

  private BitJynx(TreeMap<Long, IBlock> blocks) {
    this._blockMap = blocks;
    this._totalBits = calcTotalBits();
    this._maxBitPosition = calcMaxBitPos();
  }

  public BitJynx(long[] bitPositions) {
    // sort it
    Arrays.parallelSort(bitPositions);
    long[] unique = LongStream.of(bitPositions).distinct().toArray();
//    _totalBits = unique.length;
//    _maxBitPosition = unique[unique.length - 1];
    _blockMap = new TreeMap<>();
    storeData(unique);
    _totalBits = calcTotalBits();
    _maxBitPosition = calcMaxBitPos();
  }

  private long calcTotalBits() {
    return _blockMap.values().stream().mapToLong(IBlock::cardinality).sum();
  }

  private long calcMaxBitPos() {
    Map.Entry<Long, IBlock> lastEntry = this._blockMap.lastEntry();
    if (lastEntry == null)
      return 0;
    else {
      IBlock b = lastEntry.getValue();
      return lastEntry.getKey() * BITS_PER_BLOCK + b.lastBitPos();
    }
  }
  /**
   *
   * @param bits - bit position array, must be sorted!!!
   */
  private void storeData(long[] bits) {
    short[] tempBlock = new short[BITS_PER_BLOCK];
    long blockStart = 0;
    int bitIdx = 0;
    for(long bitNo: bits) {
      while(bitNo >= blockStart + BITS_PER_BLOCK) {
        if (bitIdx > 0) {
          // Store previous block first
          short[] block = new short[bitIdx];
          System.arraycopy(tempBlock, 0, block, 0, bitIdx);
          long blockIdx = blockStart >> BIT_BLOCK_POWER;
          _blockMap.put(blockIdx, bitIdx == BITS_PER_BLOCK ? _fullBlock : new BitPosBlock(block));
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
      _blockMap.put(blockIdx, bitIdx == BITS_PER_BLOCK ? _fullBlock : new BitPosBlock(block));
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

  public BitJynx and(BitJynx v) {
    BitJynx smallest = v._blockMap.size() < this._blockMap.size() ? v : this;
    final BitJynx largest = v._blockMap.size() < this._blockMap.size() ? this : v;

    LinkedList<RecursiveTask<Map.Entry<Long, IBlock>>> taskList = new LinkedList<>();
    smallest._blockMap.forEach( (k, x) -> {
      IBlock b = largest._blockMap.get(k);
      if (b != null) {
        taskList.add(x.andTask(k, b));
      }
    });
    TreeMap<Long, IBlock> resultMap = ForkJoinTask.invokeAll(taskList)
        .stream()
        .map(ForkJoinTask::join)
        .filter(e -> e.getValue() != null) // ignore empty blocks
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> x,  TreeMap::new));
    return new BitJynx(resultMap);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(_totalBits);
    sb.append(", highest bit: ").append(_maxBitPosition);
    if (_blockMap != null) {
      final AtomicLong counter = new AtomicLong();
      sb.append(", blocks: ").append(_blockMap.size());//.append("\n");
//      _blockMap.forEach((k, v) -> { /*sb.append(k).append(": ").append(v.toString()).append("\n"); */counter.addAndGet(v.size()); });
//      sb.append("Total block size: ").append(counter.get());
    }
    return sb.toString();
  }
}
