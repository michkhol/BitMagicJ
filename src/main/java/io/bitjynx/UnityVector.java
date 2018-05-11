package io.bitjynx;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

public class UnityVector extends AbstractVector {

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   */
  UnityVector(long[] sortedUnique) {
    super(sortedUnique, Type.ONE);
  }

  /**
   * Creates a new bit vector from a supplier
   *
   * @param supplier must provide a sorted unique array of long
   */
  public UnityVector(LongArraySupplier supplier) {
    this(supplier.getAsLongArray());
  }

  /**
   * For internal use only!!!
   * @param blocks
   */
  private UnityVector(ArrayList<NumberedBlock> blocks) {
    super(blocks, Type.ONE);
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
  public AbstractVector and(AbstractVector v) {
    switch(v.getType()) {
      case ONE:
      case ZERO:
        return new UnityVector(andLike(v._blockArray));
      default:
        throw new RuntimeException("Invalid AbstractVector type");
    }
  }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public AbstractVector or(AbstractVector v) {
    switch (v.getType()) {
      case ONE:
        return new UnityVector(orLike(v._blockArray));
      case ZERO:
        return new ZeroVector(orZeroLike(v._blockArray));
      default:
        throw new RuntimeException("Invalid AbstractVector type");
    }
  }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public AbstractVector xor(AbstractVector v) {
    switch (v.getType()) {
      case ONE:
        return new UnityVector(xorLike(v._blockArray));
      default:
        throw new RuntimeException("Invalid AbstractVector type");
    }
  }

  /**
   * Optimizes vector storage, may be slow.
   */
  public AbstractVector optimize() {
    // TODO: Optimize block storage
    return new UnityVector(_blockArray);
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

}
