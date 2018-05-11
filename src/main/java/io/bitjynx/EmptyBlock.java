package io.bitjynx;

import java.util.stream.IntStream;

import static io.bitjynx.BitJynx.BITS_PER_BLOCK;

final class EmptyBlock implements IBlock {

  public final static EmptyBlock instance = new EmptyBlock();

  private EmptyBlock() {}

  @Override
  public Type getType() { return Type.EMPTY_BLOCK; }

  @Override
  public int cardinality() { return 0; }

  @Override
  public boolean exists(int pos) { return false; }

  @Override
  public int lastBitPos() { return 0; }

  @Override
  public IntStream stream() { return IntStream.empty(); }

  @Override
  public IBlock not() {
    return UnityBlock.instance;
  }

  @Override
  public IBlock and(IBlock v2) {
    return instance;
  }

  @Override
  public IBlock or(IBlock v2) {
    return v2;
  }

  @Override
  public IBlock xor(IBlock v2) {
    return v2;
  }

  @Override
  public IBlock nand(IBlock v2) {
    return UnityBlock.instance;
  }

  @Override
  public String toString() {
    return "EmptyBlock size " + BITS_PER_BLOCK + ": [ empty ]";
  }

  @Override
  public int hashCode() { return 0; }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EmptyBlock))
      return false;
    else if (this != o)
      throw new RuntimeException("Only a single instance of EmptyBlock is allowed.");
    else
      return true;
  }
}
