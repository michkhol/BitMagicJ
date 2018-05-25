package io.bitjynx;

import java.util.ArrayList;
import java.util.stream.IntStream;

final class UnityPosVector extends AbstractPosVector {

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   * @param size only the first <code>size</code> positions will be read.
   */
  UnityPosVector(int[] sortedUnique, int size) {
    super(sortedUnique, size/*, Type.ONE*/);
  }

  /**
   * Creates a new empty bit vector
   *
   */
  UnityPosVector() {
    super();
  }

  /**
   * For internal use only!!!
   * @param blocks
   */
  UnityPosVector(ArrayList<NumberedBlock> blocks) {
    super(blocks/*, Type.ONE*/);
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
    return ((AbstractPosVector)v).andOp(this);
//    switch(v.getType()) {
//      case ONE:
//      case ZERO:
//        return new UnityPosVector(intersect(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractPosVector type");
//    }
  }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector or(IVector v) {
    return ((AbstractPosVector)v).orOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new UnityPosVector(union(v._blockArray));
//      case ZERO:
//        return new ZeroPosVector(unionZero(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractPosVector type");
//    }
  }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector xor(IVector v) {
    return ((AbstractPosVector)v).xorOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new UnityPosVector(exDisjunction(v._blockArray));
//      case ZERO:
//        return new ZeroPosVector(exDisjunctionZero(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractPosVector type");
//    }
  }

  /**
   * Performs NAND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector nand(IVector v) {
    return ((AbstractPosVector)v).nandOp(this);
  }

  /**
   * Performs AND NOT (SUB) operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector sub(IVector v) {
    return ((AbstractPosVector)v).subOp(this);
  }

  /**
   * Optimizes vector storage, may be slow.
   */
  @Override
  public IVector optimize() {
    // TODO: Optimize block storage
    return new UnityPosVector(_blockArray);
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

  @Override
  protected IVector andOp(UnityPosVector v) {
    return new UnityPosVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector andOp(ZeroPosVector v) {
    return new UnityPosVector(intersectZero(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector orOp(UnityPosVector v) {
    return new UnityPosVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector orOp(ZeroPosVector v) {
    return new ZeroPosVector(unionZero(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector xorOp(UnityPosVector v) {
    return new UnityPosVector(exDisjunction(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector xorOp(ZeroPosVector v) {
    return new ZeroPosVector(exDisjunctionZero(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector nandOp(UnityPosVector v) {
    return new ZeroPosVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector nandOp(ZeroPosVector v) {
    return new ZeroPosVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector subOp(UnityPosVector v) {
    return new UnityPosVector(intersectZero(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector subOp(ZeroPosVector v) {
    return new UnityPosVector(intersect(this._blockArray, v._blockArray));
  }



}
