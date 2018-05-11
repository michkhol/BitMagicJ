package io.bitjynx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public abstract class AbstractVector {
  final static int BIT_BLOCK_POWER = 13; // 8K blocks
  final static int BITS_PER_BLOCK = 1 << BIT_BLOCK_POWER;

  public enum Type { ZERO, ONE }

  protected final ArrayList<NumberedBlock> _blockArray;
  private final Type _type;

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   */
  public AbstractVector(long[] sortedUnique, Type t) {
    this._type = t;
    this._blockArray = new ArrayList<>();
    storeData(sortedUnique);
  }

  /**
   * For internal use only!!!
   * @param blocks
   */
  protected AbstractVector(ArrayList<NumberedBlock> blocks, Type t) {
    this._type = t;
    this._blockArray = new ArrayList<>(blocks);
  }


  Type getType() { return _type; }

  /**
   * Calculates cardinality
   *
   * @return number of bits in the vector
   */
  public abstract long cardinality();

  /**
   * Calculates the position of the highest bit
   *
   * @return highest bit position
   */
  public abstract long getMaxBitPosition();

  /**
   * Returns the bit value at the specified position
   *
   * @param idx bit position
   * @return true or false
   */
  public abstract boolean get(long idx);

  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public abstract AbstractVector and(AbstractVector v);

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public abstract AbstractVector or(AbstractVector v);

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public abstract AbstractVector xor(AbstractVector v);
  /**
   * Optimizes vector storage, may be slow.
   */
  public abstract AbstractVector optimize();

  /**
   * Deserializes into LongStream
   *
   * @return stream of sorted bit positions
   */
  public abstract LongStream stream();

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

  //////////////////////////////////////////// Internal implementation /////////////////////////////////////////////////

  protected ArrayList<NumberedBlock> andLike(ArrayList<NumberedBlock> ba) {
    ArrayList<NumberedBlock> resultArray = new ArrayList<>();
    int i = 0, j = 0;

    while (i < _blockArray.size() && j < ba.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = ba.get(j);
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
    return resultArray;
  }

  protected ArrayList<NumberedBlock> orLike(ArrayList<NumberedBlock> ba) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)this._blockArray.size() + ba.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < _blockArray.size() && j < ba.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = ba.get(j);
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
    while(j < ba.size()) {
      resultArray.add(ba.get(j++));
    }
    return resultArray;
  }

  protected ArrayList<NumberedBlock> orZeroLike(ArrayList<NumberedBlock> zBlocks) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)this._blockArray.size() + zBlocks.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < _blockArray.size() && j < zBlocks.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = zBlocks.get(j);
      if (left.no < right.no) {
        i++;
      }
      else if (right.no < left.no) {
        j++;
      }
      else {
        IBlock b = left.orOp(right);
        if (b != UnityBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    return resultArray;
  }

  protected ArrayList<NumberedBlock> xorLike(ArrayList<NumberedBlock> ba) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)this._blockArray.size() + ba.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < _blockArray.size() && j < ba.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = ba.get(j);
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
    while(j < ba.size()) {
      resultArray.add(ba.get(j++));
    }
    return resultArray;
  }

  protected ArrayList<NumberedBlock> xorZeroLike(ArrayList<NumberedBlock> ba) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)this._blockArray.size() + ba.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < _blockArray.size() && j < ba.size()) {
      NumberedBlock left = this._blockArray.get(i);
      NumberedBlock right = ba.get(j);
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
    while(j < ba.size()) {
      resultArray.add(ba.get(j++));
    }
    return resultArray;
  }


  /**
   *
   * @param bits - bit position array, must be sorted!!!
   */
  protected void storeData(long[] bits) {
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
          _blockArray.add(new NumberedBlock(blockIdx, bitIdx == BITS_PER_BLOCK ? UnityBlock.instance : new BitPosBlock(block)));
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
      _blockArray.add(new NumberedBlock(blockIdx, bitIdx == BITS_PER_BLOCK ? UnityBlock.instance : new BitPosBlock(block)));
    }
  }

  // Taken from java.util.Collections
  protected static NumberedBlock indexedBinarySearch(List<NumberedBlock> list, int key) {
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

