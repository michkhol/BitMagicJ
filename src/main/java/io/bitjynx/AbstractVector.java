package io.bitjynx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public abstract class AbstractVector {
  final static int BIT_BLOCK_POWER = 13; // 8K blocks
  final static int BITS_PER_BLOCK = 1 << BIT_BLOCK_POWER;

//  public enum Type { ZERO, ONE }

  protected final ArrayList<NumberedBlock> _blockArray;
//  private final Type _type;

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   */
  public AbstractVector(int[] sortedUnique/*, Type t*/) {
//    this._type = t;
    this._blockArray = new ArrayList<>();
    storeData(sortedUnique);
  }

  /**
   * For internal use only!!!
   * @param blocks
   */
  protected AbstractVector(ArrayList<NumberedBlock> blocks/*, Type t*/) {
//    this._type = t;
    this._blockArray = new ArrayList<>(blocks);
  }


//  Type getType() { return _type; }

  /**
   * Calculates cardinality
   *
   * @return number of bits in the vector
   */
  public abstract int cardinality();

  /**
   * Calculates the position of the highest bit
   *
   * @return highest bit position
   */
  public abstract int getMaxBitPosition();

  /**
   * Returns the bit value at the specified position
   *
   * @param idx bit position
   * @return true or false
   */
  public abstract boolean get(int idx);

  /**
   * Performs NOT operation and returns a new vector
   *
   * @return a new bit vector
   */
  public abstract AbstractVector not();

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
   * Performs NAND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public abstract AbstractVector nand(AbstractVector v);

  /**
   * Performs AND NOT operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public abstract AbstractVector sub(AbstractVector v);

  /**
   * Optimizes vector storage, may be slow.
   */
  public abstract AbstractVector optimize();

  /**
   * Deserializes the first <code>limit</code> bits into IntStream
   *
   * @param limit number of bits to stream
   * @return stream of sorted bit positions
   */
  public abstract IntStream stream(int limit);

  /**
   * Deserializes the whole vector into IntStream
   *
   * @return stream of sorted bit positions
   */
  public abstract IntStream stream();

  /**
   * Converts to array of int
   *
   * @return a sorted array of bit positions
   */
  public int[] toArray() {
    return stream().toArray();
  }

  /**
   * Converts the first <code>limit</code> bits to array of int
   *
   * @param limit number of bits to convert
   * @return a sorted array of bit positions
   */
  public int[] toArray(int limit) {
    return stream(limit).toArray();
  }

  //////////////////////////////////////////// Internal implementation /////////////////////////////////////////////////

  /**
   * Performs intersect operation between two block arrays of the same type
   *
   * @param ba1 block array
   * @param ba2 block array
   * @return always unity block array
   */
  protected static ArrayList<NumberedBlock> intersect(ArrayList<NumberedBlock> ba1, ArrayList<NumberedBlock> ba2) {
    ArrayList<NumberedBlock> resultArray = new ArrayList<>();
    int i = 0, j = 0;

    while (i < ba1.size() && j < ba2.size()) {
      NumberedBlock left = ba1.get(i);
      NumberedBlock right = ba2.get(j);
      if (left.no < right.no)
        i++;
      else if (right.no < left.no)
        j++;
      else {
        IBlock b = left.block.and(right.block); // Returns BitPosBlock regardless of 'right' argument type
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    return resultArray;
  }

  /**
   * Performs intersect operation between two block arrays of different types
   *
   * @param ba block array
   * @param bz block array
   * @return always unity block array
   */
  protected static ArrayList<NumberedBlock> intersectZero(ArrayList<NumberedBlock> ba, ArrayList<NumberedBlock> bz) {
    ArrayList<NumberedBlock> resultArray = new ArrayList<>();
    int i = 0, j = 0;

    while (i < ba.size() && j < bz.size()) {
      NumberedBlock left = ba.get(i);
      NumberedBlock right = bz.get(j);
      if (left.no < right.no) {
        resultArray.add(left);
        i++;
      }
      else if (right.no < left.no)
        j++;
      else {
        IBlock b = left.block.and(right.block); // Returns BitPosBlock regardless of 'right' argument type
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    return resultArray;
  }

  /**
   * Performs union operation between two block arrays of the same type
   *
   * @param ba1 block array
   * @param ba2 block array
   * @return always zero block array.
   */
  protected static ArrayList<NumberedBlock> union(ArrayList<NumberedBlock> ba1, ArrayList<NumberedBlock> ba2) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)ba1.size() + ba2.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < ba1.size() && j < ba2.size()) {
      NumberedBlock left = ba1.get(i);
      NumberedBlock right = ba2.get(j);
      if (left.no < right.no) {
        resultArray.add(left);
        i++;
      }
      else if (right.no < left.no) {
        resultArray.add(right);
        j++;
      }
      else {
        resultArray.add(new NumberedBlock(left.no, left.block.or(right.block)));
        i++;
        j++;
      }
    }
    // Add remaining blocks
    while(i < ba1.size()) {
      resultArray.add(ba1.get(i++));
    }
    while(j < ba2.size()) {
      resultArray.add(ba2.get(j++));
    }
    return resultArray;
  }

  /**
   * Performs union operation between block arrays of different types
   * <code>this</code> must contain a unitiy block array.
   *
   * @param ba always zero block array.
   * @param bz always zero block array.
   * @return always zero block array.
   */
  protected static ArrayList<NumberedBlock> unionZero(ArrayList<NumberedBlock> ba, ArrayList<NumberedBlock> bz) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)ba.size() + bz.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < ba.size() && j < bz.size()) {
      NumberedBlock left = ba.get(i);
      NumberedBlock right = bz.get(j);
      if (left.no < right.no) {
        i++;
      }
      else if (right.no < left.no) {
        j++;
      }
      else {
        IBlock b = left.block.or(right.block);
        if (b != UnityBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    return resultArray;
  }

  /**
   * Performs exclusive disjunction (XOR) operation between two block arrays of the same type
   *
   * @param ba1 block array
   * @param ba2 block array
   * @return always unity block array
   */
  protected static ArrayList<NumberedBlock> exDisjunction(ArrayList<NumberedBlock> ba1, ArrayList<NumberedBlock> ba2) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)ba1.size() + ba2.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < ba1.size() && j < ba2.size()) {
      NumberedBlock left = ba1.get(i);
      NumberedBlock right = ba2.get(j);
      if (left.no < right.no) {
        resultArray.add(left);
        i++;
      }
      else if (right.no < left.no) {
        resultArray.add(right);
        j++;
      }
      else {
        IBlock b = left.block.xor(right.block);
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    // Add remaining blocks
    while(i < ba1.size()) {
      resultArray.add(ba1.get(i++));
    }
    while(j < ba2.size()) {
      resultArray.add(ba2.get(j++));
    }
    return resultArray;
  }

  /**
   * Performs exclusive disjunction (XOR) operation between two block arrays of different types
   *
   * @param ba zero block array
   * @param bz zero block array
   * @return zero block array
   */
  protected static ArrayList<NumberedBlock> exDisjunctionZero(ArrayList<NumberedBlock> ba, ArrayList<NumberedBlock> bz) {
    ArrayList<NumberedBlock> resultArray =
        new ArrayList<>((int)Long.min((long)ba.size() + bz.size(), Integer.MAX_VALUE));
    int i = 0, j = 0;

    while (i < ba.size() && j < bz.size()) {
      NumberedBlock left = ba.get(i);
      NumberedBlock right = bz.get(j);
      if (left.no < right.no) {
//        resultArray.add(left);
        i++;
      }
      else if (right.no < left.no) {
        resultArray.add(right);
        j++;
      }
      else {
        IBlock b = left.block.xor(right.block);
        if (b != UnityBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        i++;
        j++;
      }
    }
    // Add remaining blocks
    while(j < bz.size()) {
      resultArray.add(bz.get(j++));
    }
    return resultArray;
  }


  /**
   *
   * @param bits - bit position array, must be sorted!!!
   */
  protected void storeData(int[] bits) {
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

//  // Using polymorphism to tell one type from another
  protected abstract AbstractVector andOp(UnityVector v);

  protected abstract AbstractVector andOp(ZeroVector v);

  protected abstract AbstractVector orOp(UnityVector v);

  protected abstract AbstractVector orOp(ZeroVector v);

  protected abstract AbstractVector xorOp(UnityVector v);

  protected abstract AbstractVector xorOp(ZeroVector v);

  protected abstract AbstractVector nandOp(UnityVector v);

  protected abstract AbstractVector nandOp(ZeroVector v);

  protected abstract AbstractVector subOp(UnityVector v);

  protected abstract AbstractVector subOp(ZeroVector v);
}

