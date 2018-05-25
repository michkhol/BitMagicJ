package io.bitjynx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static io.bitjynx.BitMapBlock.CELL_POWER;

final class BitMapVector implements IVector {
  final static int BIT_BLOCK_POWER = BitJynx.BIT_BLOCK_POWER;
  final static int BITS_PER_BLOCK = BitJynx.BITS_PER_BLOCK;


  private final ArrayList<NumberedBlock> _blockArray;

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   * @param size only the first <code>size</code> positions will be read.
   */
  BitMapVector(int[] sortedUnique, int size) {
    _blockArray = storeData(sortedUnique, size);
  }

  /**
   * Creates a new empty bit vector
   *
   */
  BitMapVector() {
    _blockArray = new ArrayList<>();
  }

  /**
   * For internal use only!!!
   * @param blocks
   */
  BitMapVector(ArrayList<NumberedBlock> blocks) {
    _blockArray = blocks;
  }

  /**
   * Calculates cardinality
   *
   * @return number of bits in the vector
   */
  @Override
  public int cardinality() {
    if (_blockArray != null )
      return _blockArray.stream().mapToInt(x -> x.block.cardinality()).sum();
    else
      return 0;
  }

  /**
   * Calculates the position of the highest bit
   *
   * @return highest bit position
   */
  @Override
  public int getMaxBitPosition() {
    if (this._blockArray != null && !this._blockArray.isEmpty()) {
      NumberedBlock lastEntry = this._blockArray.get(this._blockArray.size() - 1);
      if (lastEntry == null)
        return 0;
      else {
        return (lastEntry.no << BIT_BLOCK_POWER) + lastEntry.block.lastBitPos();
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
  @Override
  public boolean get(int idx) {
    // find the block
    int blockIdx = idx >> BIT_BLOCK_POWER;
    NumberedBlock found = indexedBinarySearch(_blockArray, blockIdx);
    if (found == null) return false;
    else {
      int pos = idx - (blockIdx << BIT_BLOCK_POWER);
      return found.block.exists(pos);
    }
  }

  @Override
  public IVector not() {
    ArrayList<NumberedBlock> al = new ArrayList<>(this._blockArray.size());
    for(NumberedBlock nb : this._blockArray) {
      al.add(new NumberedBlock(nb.no, nb.block.not()));
    }
    return new ZeroPosVector(al);
  }

  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector and(IVector v) {
    return new BitMapVector(intersect(_blockArray, ((BitMapVector)v)._blockArray));
  }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector or(IVector v) {
    return new BitMapVector(union(_blockArray, ((BitMapVector)v)._blockArray));
  }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector xor(IVector v) {
    return new BitMapVector(exDisjunction(_blockArray, ((BitMapVector)v)._blockArray));
  }

  /**
   * Performs NAND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector nand(IVector v) {
    return null;
  }

  /**
   * Performs AND NOT (SUB) operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector sub(IVector v) {
    return null;
  }

  /**
   * Optimizes vector storage, may be slow.
   */
  @Override
  public IVector optimize() {
    // TODO: Optimize block storage
    return new BitMapVector(_blockArray);
  }

  @Override
  public IntStream stream() {
    return _blockArray.stream()
        .flatMapToInt(nb -> {
          int offset = nb.no << BIT_BLOCK_POWER;
          return nb.block.stream().map(x -> offset + x );
        });
  }

  @Override
  public IntStream stream(int limit) {
    return _blockArray.stream()
        .flatMapToInt(nb -> {
          int offset = nb.no << BIT_BLOCK_POWER;
          return nb.block.stream().map(x -> offset + x );
        }).filter(x -> x < limit);
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
        i++;
      else if (right.no < left.no)
        j++;
      else {
        IBlock b = left.block.and(right.block);
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
   * Creates the block array
   *
   * @param positions - bit position array, must be sorted!!!
   * @return new block array
   */
  private ArrayList<NumberedBlock> storeData(int[] positions, int size) {
    ArrayList<NumberedBlock> blockArray = new ArrayList<>();
    long[] map = new long[BITS_PER_BLOCK >> CELL_POWER];
    int blockStart = 0;
    int bitIdx = 0;
    for(int i = 0; i < size; ++i) {
      int globalPos = positions[i];
      while(globalPos >= blockStart + BITS_PER_BLOCK) {
        if (bitIdx > 0) {
          // Store previous map first
          int blockIdx = blockStart >> BIT_BLOCK_POWER;
          blockArray.add(new NumberedBlock(blockIdx, bitIdx == BITS_PER_BLOCK ? UnityBlock.instance : new BitMapBlock(map)));
          map = new long[BITS_PER_BLOCK >> CELL_POWER];
          bitIdx = 0;
        }
        blockStart += BITS_PER_BLOCK;
      }
      int pos = globalPos - blockStart;
      BitMapBlock.setBit(map, pos);
      ++bitIdx;
    }
    if (bitIdx > 0) {
      // Store the rest
      int blockIdx = blockStart >> BIT_BLOCK_POWER;
      blockArray.add(new NumberedBlock(blockIdx, bitIdx == BITS_PER_BLOCK ? UnityBlock.instance : new BitMapBlock(map)));
    }
    return blockArray;
  }

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
    return null;  // key not found
  }


}
