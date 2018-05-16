package io.bitjynx;

import java.util.stream.IntStream;

public final class BitJynx {
  final static int BIT_BLOCK_POWER = AbstractVector.BIT_BLOCK_POWER;
  final static int BITS_PER_BLOCK = AbstractVector.BITS_PER_BLOCK;

  private final AbstractVector _internal;

  /**
   * Creates a new bit vector from array of bit positions
   *
   * @param sortedUnique must contain sorted unique elements, otherwise the behavior is undefined.
   */
  public BitJynx(int[] sortedUnique) {
    _internal = new UnityVector(sortedUnique);
  }

  /**
   * Creates a new bit vector from a supplier
   *
   * @param supplier must provide a sorted unique array of long
   */
  public BitJynx(IntArraySupplier supplier) {
    _internal = new UnityVector(supplier.getAsIntArray());
  }

  /**
   * For internal use only!!!
   * @param v
   */
  private BitJynx(AbstractVector v) {
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
  public String toString() { return _internal.toString(); }

}
