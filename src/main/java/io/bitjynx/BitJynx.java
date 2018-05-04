package io.bitjynx;

import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

// TODO: Invert the whole vector for SUB operation
public class BitJynx {
  final static int BIT_BLOCK_POWER = 13; // 8K blocks
  final static int BITS_PER_BLOCK = 1 << BIT_BLOCK_POWER;

  private final ArrayList<NumberedBlock> _blockArray;


  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   */
  public BitJynx(long[] sortedUnique) {
    _blockArray = new ArrayList<>();
    storeData(sortedUnique);
  }

  /**
   * Creates a new bit vector from a supplier
   *
   * @param supplier must provide a sorted unique array of long
   */
  public BitJynx(LongArraySupplier supplier) {
    this(supplier.getAsLongArray());
  }

  /**
   * For internal use only!!!
   * @param blocks
   */
  private BitJynx(ArrayList<NumberedBlock> blocks) {
    this._blockArray = new ArrayList<>(blocks);
  }

  /**
   * Calculates cardinality
   *
   * @return number of bits in the vector
   */
  public long cardinality() {
    if (_blockArray != null )
      return _blockArray.stream().mapToLong(x -> x.block.cardinality()).sum();
    else
      return 0;
  }

  /**
   * Calculates the position of the highest bit
   *
   * @return highest bit position
   */
  public long getMaxBitPosition() {
    if (this._blockArray != null && !this._blockArray.isEmpty()) {
      NumberedBlock lastEntry = this._blockArray.get(this._blockArray.size() - 1);
      if (lastEntry == null)
        return 0;
      else {
        return ((long)lastEntry.no << BIT_BLOCK_POWER) + lastEntry.block.lastBitPos();
      }
    }
    else
      return 0;
  }

  /**
   * Returns the bit value at the specified position
   *
   * @param idx bit position
   * @return true or false
   */
  public boolean get(long idx) {
    // find the block
    long blockIdx = idx >> BIT_BLOCK_POWER;
    if (blockIdx > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Index is out of range");
    NumberedBlock found = indexedBinarySearch(_blockArray, (int)blockIdx);
    if (found == null) return false;
    else {
      int pos = (int)(idx - (blockIdx << BIT_BLOCK_POWER));
      return found.block.exists(pos);
    }
  }

  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public BitJynx and(BitJynx v) {
    ArrayList<NumberedBlock> resultArray = new ArrayList<>();
    int i = 0, j = 0;

    while (i < _blockArray.size() && j < v._blockArray.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = v._blockArray.get(j);
      if (left.no < right.no)
        i++;
      else if (right.no < left.no)
        j++;
      else {
        IBlock b = left.andOp(right);
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    return new BitJynx(resultArray);
  }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public BitJynx or(BitJynx v) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)this._blockArray.size() + v._blockArray.size(), Integer.MAX_VALUE));
    int i = 0, j = 0, counter = 0;

    while (i < _blockArray.size() && j < v._blockArray.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = v._blockArray.get(j);
      if (left.no < right.no) {
        resultArray.add(left);
        i++;
      }
      else if (right.no < left.no) {
        resultArray.add(right);
        j++;
      }
      else {
        resultArray.add(new NumberedBlock(left.no, left.orOp(right)));
        i++;
        j++;
      }
    }
    // Add remaining blocks
    while(i < _blockArray.size()) {
      resultArray.add(this._blockArray.get(i++));
    }
    while(j < v._blockArray.size()) {
      resultArray.add(v._blockArray.get(j++));
    }
    return new BitJynx(resultArray);
  }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public BitJynx xor(BitJynx v) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)this._blockArray.size() + v._blockArray.size(), Integer.MAX_VALUE));
    int i = 0, j = 0, counter = 0;

    while (i < _blockArray.size() && j < v._blockArray.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = v._blockArray.get(j);
      if (left.no < right.no) {
        resultArray.add(left);
        i++;
      }
      else if (right.no < left.no) {
        resultArray.add(right);
        j++;
      }
      else {
        IBlock b = left.xorOp(right);
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    // Add remaining blocks
    while(i < _blockArray.size()) {
      resultArray.add(this._blockArray.get(i++));
    }
    while(j < v._blockArray.size()) {
      resultArray.add(v._blockArray.get(j++));
    }
    return new BitJynx(resultArray);
  }

  /**
   * Optimizes vector storage, may be slow.
   */
  public BitJynx optimize() {
    // TODO: Optimize block storage
    return new BitJynx(_blockArray);
  }

  /**
   * Deserializes into LongStream
   *
   * @return stream of sorted bit positions
   */
  public LongStream stream() {
    return _blockArray.stream()
        .flatMapToLong(nb -> {
          return nb.block.stream().mapToLong(x -> ((long)nb.no << BIT_BLOCK_POWER) + x);
        });
  }

  /**
   * Converts to array of long, limited by the index range (0..Integer.MAX_VALUE)
   *
   * @return a sorted array of bit positions
   */
  public long[] toArray() {
    long size = cardinality();
    if (size > Integer.MAX_VALUE)
      throw new RuntimeException("Will not fit into an array due to index constraint");
    return stream().toArray();
  }



  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(cardinality());
    sb.append(", highest bit: ").append(getMaxBitPosition());
    if (_blockArray != null) {
      final AtomicLong counter = new AtomicLong();
      sb.append(", blocks: ").append(_blockArray.size());//.append("\n");
//      _blockMap.forEach((k, v) -> { /*sb.append(k).append(": ").append(v.toString()).append("\n"); */counter.addAndGet(v.size()); });
//      sb.append("Total block size: ").append(counter.getAsLongArray());
    }
    return sb.toString();
  }

  //////////////////////////////////////////// Internal implementation /////////////////////////////////////////////////

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
      _blockArray.add(new NumberedBlock(blockIdx, bitIdx == BITS_PER_BLOCK ? FullBlock.instance : new BitPosBlock(block)));
    }
  }

  //  private BitJynx createFrom(LinkedList<RecursiveTask<NumberedBlock>> taskList, ArrayList<NumberedBlock> blockArray) {
//    ArrayList<NumberedBlock> resultArray = ForkJoinTask.invokeAll(taskList)
//        .stream()
//        .map(ForkJoinTask::join)
//        .filter(e -> e.block != null) // drop empty blocks
//        .collect(Collectors.toCollection(() -> blockArray));
//    return new BitJynx(resultArray);
//  }

  // Taken from java.util.Collections
  private static NumberedBlock indexedBinarySearch(List<NumberedBlock> list, int key) {
    int low = 0;
    int high = list.size() - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      NumberedBlock midVal = list.get(mid);
      int cmp = midVal.no - key;

      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        high = mid - 1;
      else
        return midVal; // key found
    }
//    return -(low + 1);  // key not found
    return null;  // key not found
  }

}
