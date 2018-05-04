package io.bitjynx;

import java.util.stream.IntStream;

import static io.bitjynx.BitJynx.BITS_PER_BLOCK;

class EmptyBlock implements IBlock {

  public final static EmptyBlock instance = new EmptyBlock();

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
    return FullBlock.instance;
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
  public IBlock sub(IBlock v2) {
    return FullBlock.instance;
  }

  @Override
  public String toString() {
    return "EmptyBlock size " + BITS_PER_BLOCK + ": [ empty ]";
  }
}
