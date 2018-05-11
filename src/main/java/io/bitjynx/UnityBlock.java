package io.bitjynx;

import java.util.stream.IntStream;

import static io.bitjynx.BitJynx.BITS_PER_BLOCK;

final class UnityBlock implements IBlock {

  public final static UnityBlock instance = new UnityBlock();

  private UnityBlock() {}

  @Override
  public Type getType() { return Type.UNITY_BLOCK; }

  @Override
  public int cardinality() { return BITS_PER_BLOCK; }

  @Override
  public boolean exists(int pos) { return true; }

  @Override
  public int lastBitPos() { return BITS_PER_BLOCK - 1; }

  @Override
  public IntStream stream() { return IntStream.range(0, BITS_PER_BLOCK); }

  @Override
  public IBlock not() {
    return EmptyBlock.instance;
  }

  @Override
  public IBlock and(IBlock v2) {
    return v2;
  }

  @Override
  public IBlock or(IBlock v2) {
    return instance;
  }

  @Override
  public IBlock xor(IBlock v2) { return v2.not(); }

  @Override
  public IBlock nand(IBlock v2) {
    return v2.not();
  }

  @Override
  public String toString() {
    return "UnityBlock size " + BITS_PER_BLOCK + ": [ all set ]";
  }

  @Override
  public int hashCode() { return 1; }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnityBlock))
      return false;
    else if (this != o)
      throw new RuntimeException("Only a single instance of UnitBlock is allowed.");
    else
      return true;
  }
}
