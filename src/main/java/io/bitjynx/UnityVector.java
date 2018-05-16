package io.bitjynx;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public final class UnityVector extends AbstractVector {

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   */
  UnityVector(int[] sortedUnique) {
    super(sortedUnique/*, Type.ONE*/);
  }

  /**
   * Creates a new bit vector from a supplier
   *
   * @param supplier must provide a sorted unique array of long
   */
  public UnityVector(IntArraySupplier supplier) {
    this(supplier.getAsIntArray());
  }

  /**
   * For internal use only!!!
   * @param blocks
   */
  UnityVector(ArrayList<NumberedBlock> blocks) {
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
  public AbstractVector not() {
    ArrayList<NumberedBlock> al = new ArrayList<>(this._blockArray.size());
    for(NumberedBlock nb : this._blockArray) {
      al.add(new NumberedBlock(nb.no, nb.block.not()));
    }
    return new ZeroVector(al);
  }

  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector and(AbstractVector v) {
    return v.andOp(this);
//    switch(v.getType()) {
//      case ONE:
//      case ZERO:
//        return new UnityVector(intersect(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractVector type");
//    }
  }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector or(AbstractVector v) {
    return v.orOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new UnityVector(union(v._blockArray));
//      case ZERO:
//        return new ZeroVector(unionZero(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractVector type");
//    }
  }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector xor(AbstractVector v) {
    return v.xorOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new UnityVector(exDisjunction(v._blockArray));
//      case ZERO:
//        return new ZeroVector(exDisjunctionZero(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractVector type");
//    }
  }

  /**
   * Performs NAND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector nand(AbstractVector v) {
    return v.nandOp(this);
  }

  /**
   * Performs AND NOT (SUB) operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector sub(AbstractVector v) {
    return v.subOp(this);
  }

  /**
   * Optimizes vector storage, may be slow.
   */
  @Override
  public AbstractVector optimize() {
    // TODO: Optimize block storage
    return new UnityVector(_blockArray);
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
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(cardinality());
    sb.append(", highest bit: ").append(getMaxBitPosition());
    if (_blockArray != null) {
      final AtomicLong counter = new AtomicLong();
      sb.append(", blocks: ").append(_blockArray.size());//.append("\n");
//      _blockMap.forEach((k, v) -> { /*sb.append(k).append(": ").append(v.toString()).append("\n"); */counter.addAndGet(v.size()); });
//      sb.append("Total block size: ").append(counter.getAsIntArray());
    }
    return sb.toString();
  }

  @Override
  protected AbstractVector andOp(UnityVector v) {
    return new UnityVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector andOp(ZeroVector v) {
    return new UnityVector(intersectZero(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector orOp(UnityVector v) {
    return new UnityVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector orOp(ZeroVector v) {
    return new ZeroVector(unionZero(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector xorOp(UnityVector v) {
    return new UnityVector(exDisjunction(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector xorOp(ZeroVector v) {
    return new ZeroVector(exDisjunctionZero(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector nandOp(UnityVector v) {
    return new ZeroVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector nandOp(ZeroVector v) {
    return new ZeroVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector subOp(UnityVector v) {
    return new UnityVector(intersectZero(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector subOp(ZeroVector v) {
    return new UnityVector(intersect(this._blockArray, v._blockArray));
  }

}
