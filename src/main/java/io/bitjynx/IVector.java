package io.bitjynx;

import java.util.stream.IntStream;

interface IVector {
  /**
   * Calculates cardinality
   *
   * @return number of bits in the vector
   */
  int cardinality();

  /**
   * Calculates the position of the highest bit
   *
   * @return highest bit position
   */
  int getMaxBitPosition();

  /**
   * Returns the bit value at the specified position
   *
   * @param idx bit position
   * @return true or false
   */
  boolean get(int idx);

  /**
   * Performs NOT operation and returns a new vector
   *
   * @return a new bit vector
   */
  IVector not();

  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  IVector and(IVector v);

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  IVector or(IVector v);

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  IVector xor(IVector v);

  /**
   * Performs NAND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  IVector nand(IVector v);

  /**
   * Performs AND NOT operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  IVector sub(IVector v);

  /**
   * Optimizes vector storage, may be slow.
   */
  IVector optimize();

  /**
   * Deserializes the first <code>limit</code> bits into IntStream
   *
   * @param limit number of bits to stream
   * @return stream of sorted bit positions
   */
  IntStream stream(int limit);

  /**
   * Deserializes the whole vector into IntStream
   *
   * @return stream of sorted bit positions
   */
  IntStream stream();

  int[] toArray();

  int[] toArray(int limit);

  ////////////////////////// Internal use /////////////////////////////////////
  int blockArraySize();

  double avgCardinalityPerBlock();

}
