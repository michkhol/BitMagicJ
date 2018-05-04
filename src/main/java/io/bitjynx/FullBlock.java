package io.bitjynx;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static io.bitjynx.BitJynx.BITS_PER_BLOCK;

class FullBlock implements IBlock {

  public final static FullBlock instance = new FullBlock();

  @Override
  public Type getType() { return Type.FULL_BLOCK; }

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
  public IBlock sub(IBlock v2) {
    return v2.not();
  }

  @Override
  public String toString() {
    return "FullBlock size " + BITS_PER_BLOCK + ": [ all set ]";
  }
}
