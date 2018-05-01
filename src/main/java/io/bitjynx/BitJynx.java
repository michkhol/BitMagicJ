package io.bitjynx;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

// TODO: Rework block map to an array
public class BitJynx {
  final static int BIT_BLOCK_POWER = 13; // 8K blocks
  final static int BITS_PER_BLOCK = 1 << BIT_BLOCK_POWER;

//  private TreeMap<Long, IBlock> _blockMap;
  private ArrayList<NumberedBlock> _blockArray;

  private long _totalBits;

//  private BitJynx(TreeMap<Long, IBlock> blocks) {
//    this._blockMap = blocks;
//    this._totalBits = calcTotalBits();
//  }

  private BitJynx(ArrayList<NumberedBlock> blocks) {
    this._blockArray = blocks;
    this._totalBits = calcTotalBits();
  }

  public BitJynx(long[] bitPositions) {
    // sort it
    Arrays.parallelSort(bitPositions);
    long[] unique = LongStream.of(bitPositions).distinct().toArray();
//    _blockMap = new TreeMap<>();
    _blockArray = new ArrayList<>();
    storeData(unique);
    _totalBits = calcTotalBits();
  }

  private long calcTotalBits() {
//    return _blockMap.values().stream().mapToLong(IBlock::cardinality).sum();
    return _blockArray.stream().mapToLong(x -> x.block.cardinality()).sum();
  }

  /**
   *
   * @param bits - bit position array, must be sorted!!!
   */
  private void storeData(long[] bits) {
    short[] tempBlock = new short[BITS_PER_BLOCK];
    int blockStart = 0;
    int bitIdx = 0;
    for(long bitNo: bits) {
      while(bitNo >= blockStart + BITS_PER_BLOCK) {
        if (bitIdx > 0) {
          // Store previous block first
          short[] block = new short[bitIdx];
          System.arraycopy(tempBlock, 0, block, 0, bitIdx);
          int blockIdx = blockStart >> BIT_BLOCK_POWER;
//          _blockMap.put(blockIdx, bitIdx == BITS_PER_BLOCK ? FullBlock.instance : new BitPosBlock(block));
          _blockArray.add(new NumberedBlock(blockIdx, bitIdx == BITS_PER_BLOCK ? FullBlock.instance : new BitPosBlock(block)));
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
      int blockIdx = blockStart >> BIT_BLOCK_POWER;
//      _blockMap.put(blockIdx, bitIdx == BITS_PER_BLOCK ? FullBlock.instance : new BitPosBlock(block));
      _blockArray.add(new NumberedBlock(blockIdx, bitIdx == BITS_PER_BLOCK ? FullBlock.instance : new BitPosBlock(block)));
    }
  }

//  private BitJynx createFrom(LinkedList<RecursiveTask<Map.Entry<Long, IBlock>>> taskList, TreeMap<Long, IBlock> blockMap) {
//    TreeMap<Long, IBlock> resultMap = ForkJoinTask.invokeAll(taskList)
//        .stream()
//        .map(ForkJoinTask::join)
//        .filter(e -> e.getValue() != null) // drop empty blocks
//        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> x,  () -> blockMap));
//    return new BitJynx(resultMap);
//  }

  private BitJynx createFrom(LinkedList<RecursiveTask<NumberedBlock>> taskList, ArrayList<NumberedBlock> blockArray) {
    ArrayList<NumberedBlock> resultArray = ForkJoinTask.invokeAll(taskList)
        .stream()
        .map(ForkJoinTask::join)
        .filter(e -> e.block != null) // drop empty blocks
        .collect(Collectors.toCollection(() -> blockArray));
    return new BitJynx(resultArray);
  }

  public long cardinality() { return _totalBits; }

//  public long getMaxBitPosition() {
//    Map.Entry<Long, IBlock> lastEntry = this._blockMap.lastEntry();
//    if (lastEntry == null)
//      return 0;
//    else {
//      IBlock b = lastEntry.getValue();
//      return lastEntry.getKey() * BITS_PER_BLOCK + b.lastBitPos();
//    }
//  }

  public long getMaxBitPosition() {
    NumberedBlock lastEntry = this._blockArray.get(this._blockArray.size() - 1);
    if (lastEntry == null)
      return 0;
    else {
      return lastEntry.no * BITS_PER_BLOCK + lastEntry.block.lastBitPos();
    }
  }

//  public boolean get(long idx) {
//    // find the block
//    long blockIdx = idx >> BIT_BLOCK_POWER;
//    IBlock b = _blockMap.get(blockIdx);
//    if (b == null) return false;
//    else {
//      int pos = (int)(idx - (blockIdx << BIT_BLOCK_POWER));
//      return b.exists(pos);
//    }
//  }

  public boolean get(int idx) {
    // find the block
    int blockIdx = idx >> BIT_BLOCK_POWER;
    int found = Collections.binarySearch(_blockArray, new NumberedBlock(blockIdx, null));
    if (found < 0) return false;
    else {
      int pos = (idx - (blockIdx << BIT_BLOCK_POWER));
      return _blockArray.get(found).block.exists(pos);
    }
  }

//  public BitJynx and(BitJynx v) {
//    LinkedList<RecursiveTask<Map.Entry<Long, IBlock>>> taskList = new LinkedList<>();
//    Iterator<Map.Entry<Long, IBlock>> ileft = this._blockMap.entrySet().iterator();
//    Iterator<Map.Entry<Long, IBlock>> iright = v._blockMap.entrySet().iterator();
//
//    if (ileft.hasNext() && iright.hasNext()) {
//      Map.Entry<Long, IBlock> l = ileft.next();
//      Map.Entry<Long, IBlock> r = iright.next();
//      while (true) {
//        if (l.getKey() < r.getKey()) {
//          if (ileft.hasNext()) l = ileft.next(); else break;
//        }
//        else if (r.getKey() < l.getKey()) {
//          if (iright.hasNext()) r = iright.next(); else break;
//        }
//        else {
//          taskList.add(l.getValue().andTask(l.getKey(), r.getValue()));
//          if (ileft.hasNext()) l = ileft.next(); else break;
//          if (iright.hasNext()) r = iright.next(); else break;
//        }
//      }
//    }
//
////    BitJynx smallest = v._blockMap.size() < this._blockMap.size() ? v : this;
////    final BitJynx largest = v._blockMap.size() < this._blockMap.size() ? this : v;
//
////    LinkedList<RecursiveTask<Map.Entry<Long, IBlock>>> taskList = new LinkedList<>();
////    smallest._blockMap.forEach( (k, x) -> {
////      IBlock b = largest._blockMap.get(k);
////      if (b != null) {
////        taskList.add(x.andTask(k, b));
////      }
////    });
//
//    return createFrom(taskList, new TreeMap<>());
//  }

  public BitJynx and(BitJynx v) {
    LinkedList<RecursiveTask<NumberedBlock>> taskList = new LinkedList<>();
    int i = 0, j = 0;

    while (i < _blockArray.size() && j < v._blockArray.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = v._blockArray.get(j);
      if (left.no < right.no)
        i++;
      else if (right.no < left.no)
        j++;
      else {
        taskList.add(left.block.andTask(left.no, right.block));
        i++;
        j++;
      }
    }

//    BitJynx smallest = v._blockMap.size() < this._blockMap.size() ? v : this;
//    final BitJynx largest = v._blockMap.size() < this._blockMap.size() ? this : v;

//    LinkedList<RecursiveTask<Map.Entry<Long, IBlock>>> taskList = new LinkedList<>();
//    smallest._blockMap.forEach( (k, x) -> {
//      IBlock b = largest._blockMap.get(k);
//      if (b != null) {
//        taskList.add(x.andTask(k, b));
//      }
//    });

    return createFrom(taskList, new TreeMap<>());
  }

  public BitJynx or(BitJynx v) {
    LinkedList<RecursiveTask<Map.Entry<Long, IBlock>>> taskList = new LinkedList<>();
    TreeMap<Long, IBlock> resultMap = new TreeMap<>();
    Iterator<Map.Entry<Long, IBlock>> ileft = this._blockMap.entrySet().iterator();
    Iterator<Map.Entry<Long, IBlock>> iright = v._blockMap.entrySet().iterator();

    if (ileft.hasNext() && iright.hasNext()) {
      Map.Entry<Long, IBlock> l = ileft.next();
      Map.Entry<Long, IBlock> r = iright.next();
      while (true) {
        if (l.getKey() < r.getKey()) {
          resultMap.put(l.getKey(), l.getValue());
          if (ileft.hasNext()) l = ileft.next(); else break;
        }
        else if (r.getKey() < l.getKey()) {
          resultMap.put(r.getKey(), r.getValue());
          if (iright.hasNext()) r = iright.next(); else break;
        }
        else {
          taskList.add(l.getValue().orTask(l.getKey(), r.getValue()));
          if (ileft.hasNext()) l = ileft.next(); else break;
          if (iright.hasNext()) r = iright.next(); else break;
        }
      }
    }
    // Add remaining blocks
    while(ileft.hasNext()) {
      Map.Entry<Long, IBlock> l = ileft.next();
      resultMap.put(l.getKey(), l.getValue());
    }
    while(iright.hasNext()) {
      Map.Entry<Long, IBlock> r = iright.next();
      resultMap.put(r.getKey(), r.getValue());
    }

    return createFrom(taskList, resultMap);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(_totalBits);
    sb.append(", highest bit: ").append(getMaxBitPosition());
    if (_blockMap != null) {
      final AtomicLong counter = new AtomicLong();
      sb.append(", blocks: ").append(_blockMap.size());//.append("\n");
//      _blockMap.forEach((k, v) -> { /*sb.append(k).append(": ").append(v.toString()).append("\n"); */counter.addAndGet(v.size()); });
//      sb.append("Total block size: ").append(counter.get());
    }
    return sb.toString();
  }
}
