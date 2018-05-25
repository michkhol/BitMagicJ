package io.bitjynx;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractPosVector implements IVector {
  final static int BIT_BLOCK_POWER = 13; // 8K blocks
  final static int BITS_PER_BLOCK = 1 << BIT_BLOCK_POWER;

//  public enum Type { ZERO, ONE }

  protected final ArrayList<NumberedBlock> _blockArray;
//  private final Type _type;

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   * @param size only the first <code>size</code> positions will be read, must be less than
   *             or equal the <code>sortedUnique</code> length
   */
  AbstractPosVector(int[] sortedUnique, int size/*, Type t*/) {
//    this._type = t;
    this._blockArray = new ArrayList<>();
    storeData(sortedUnique, size);
  }

  /**
   * Creates a new empty bit vector
   *
   */
  AbstractPosVector() {
    this._blockArray = new ArrayList<>();
  }

  /**
   * For internal use only!!!
   * @param blocks block array
   */
  protected AbstractPosVector(ArrayList<NumberedBlock> blocks/*, Type t*/) {
//    this._type = t;
    this._blockArray = blocks;
  }


  /**
   * Converts to array of int
   *
   * @return a sorted array of bit positions
   */
  @Override
  public int[] toArray() {
    return stream().toArray();
  }

  /**
   * Converts the first <code>limit</code> bits to array of int
   *
   * @param limit number of bits to convert
   * @return a sorted array of bit positions
   */
  @Override
  public int[] toArray(int limit) {
    return stream(limit).toArray();
  }

  /**
   * For internal use only!!!
   *
   * @return
   */
  public int blockArraySize() { return _blockArray == null ? 0 : _blockArray.size(); }

  /**
   * For internal use only!!!
   *
   * @return
   */
  public double avgCardinalityPerBlock() {
    if (_blockArray != null )
      return _blockArray.stream().mapToDouble(x -> x.block.cardinality()).average().orElse(0.);
    else
      return 0;
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
        ++i;
      else if (right.no < left.no)
        ++j;
      else {
        IBlock b = left.block.and(right.block); // Returns BitPosBlock regardless of 'right' argument type
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        ++i;
        ++j;
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
        ++i;
      }
      else if (right.no < left.no)
        ++j;
      else {
        IBlock b = left.block.and(right.block); // Returns BitPosBlock regardless of 'right' argument type
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        ++i;
        ++j;
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
        ++i;
      }
      else if (right.no < left.no) {
        resultArray.add(right);
        ++j;
      }
      else {
        resultArray.add(new NumberedBlock(left.no, left.block.or(right.block)));
        ++i;
        ++j;
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
        ++i;
      }
      else if (right.no < left.no) {
        ++j;
      }
      else {
        IBlock b = left.block.or(right.block);
        if (b != UnityBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        ++i;
        ++j;
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
        ++j;
      }
      else {
        IBlock b = left.block.xor(right.block);
        if (b != EmptyBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        ++i;
        ++j;
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
        ++i;
      }
      else if (right.no < left.no) {
        resultArray.add(right);
        ++j;
      }
      else {
        IBlock b = left.block.xor(right.block);
        if (b != UnityBlock.instance)
          resultArray.add(new NumberedBlock(left.no, b));
        ++i;
        ++j;
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
   * @param positions - bit position array, must be sorted!!!
   */
  protected void storeData(int[] positions, int size) {
    short[] tempBlock = new short[BITS_PER_BLOCK];
    int blockStart = 0;
    int bitIdx = 0;
    for(int i = 0; i < size; ++i) {
      int globalPos = positions[i];
      while(globalPos >= blockStart + BITS_PER_BLOCK) {
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
      short pos = (short)(globalPos - blockStart); // the cast effectively makes 'unsigned short'
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
  protected abstract IVector andOp(UnityPosVector v);

  protected abstract IVector andOp(ZeroPosVector v);

  protected abstract IVector orOp(UnityPosVector v);

  protected abstract IVector orOp(ZeroPosVector v);

  protected abstract IVector xorOp(UnityPosVector v);

  protected abstract IVector xorOp(ZeroPosVector v);

  protected abstract IVector nandOp(UnityPosVector v);

  protected abstract IVector nandOp(ZeroPosVector v);

  protected abstract IVector subOp(UnityPosVector v);

  protected abstract IVector subOp(ZeroPosVector v);
}

