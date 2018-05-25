package io.bitjynx;

import java.util.stream.IntStream;

public final class BitJynx {
  final static int BIT_BLOCK_POWER = AbstractPosVector.BIT_BLOCK_POWER;
  final static int BITS_PER_BLOCK = AbstractPosVector.BITS_PER_BLOCK;

  public static BitJynx empty = new BitJynx(new UnityPosVector());

  private final IVector _internal;

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   * @param size only the first <code>size</code> positions will be read.
   */
  public BitJynx(int[] sortedUnique, int size) {
    if (size > sortedUnique.length)
      throw new IllegalArgumentException("Parameter 'size' must be less than or equal the supplied array length.");
    _internal = new UnityPosVector(sortedUnique, size);
  }

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   */
  public BitJynx(int[] sortedUnique) {
    _internal = new UnityPosVector(sortedUnique, sortedUnique.length);
  }

  /**
   * Creates a new bit vector from a supplier
   *
   * @param supplier must provide a sorted unique array of long
   */
  public BitJynx(IntArraySupplier supplier) {
    int[] a = supplier.getAsIntArray();
    _internal = new UnityPosVector(a, a.length);
  }

  /**
   * For internal use only!!!
   * @param v
   */
  private BitJynx(IVector v) {
    this._internal = v;
  }

  /**
   * Calculates cardinality
   *
   * @return number of bits in the vector
   */
  public long cardinality() { return _internal.cardinality(); }

  /**
   * Calculates the position of the highest bit
   *
   * @return highest bit position
   */
  public int getMaxBitPosition() {return _internal.getMaxBitPosition(); }

  /**
   * Returns the bit value at the specified position
   *
   * @param idx bit position
   * @return true or false
   */
  public boolean get(int idx) {return _internal.get(idx); }

  /**
   * Performs NOT operation and returns a new vector
   *
   * @return a new bit vector
   */
  public BitJynx not() { return new BitJynx(_internal.not()); }


  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public BitJynx and(BitJynx v) { return new BitJynx(_internal.and(v._internal)); }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public BitJynx or(BitJynx v) {return new BitJynx(_internal.or(v._internal)); }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  public BitJynx xor(BitJynx v) { return new BitJynx(_internal.xor(v._internal)); }

  /**
   * Optimizes vector storage, may be slow.
   */
  public BitJynx optimize() {
    return new BitJynx(_internal.optimize());
  }

  /**
   * Deserializes into IntStream.
   * The <code>limit</code> is the maximum desired bit position (exclusive).
   *
   * @param limit the highest desired bit position (exclusive)
   * @return stream of sorted bit positions
   */
  public IntStream stream(int limit) { return _internal.stream(limit);  }

  /**
   * Deserializes the whole vector into IntStream.
   * Will throw if the vector is inverted.
   *
   * @return stream of sorted bit positions
   */
  public IntStream stream() { return _internal.stream();  }

  /**
   * Converts to array of int
   *
   * @return a sorted array of bit positions
   */
  public int[] toArray() { return _internal.toArray(); }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(cardinality());
    sb.append(", highest bit: ").append(getMaxBitPosition());
    sb.append(", blocks: ").append(_internal.blockArraySize());
    sb.append(", average cardinality per block: ").append(_internal.avgCardinalityPerBlock());
    return sb.toString();
  }

}
