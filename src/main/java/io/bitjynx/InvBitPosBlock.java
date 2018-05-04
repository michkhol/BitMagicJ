package io.bitjynx;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * This class has zeroes as bit postions
 */
class InvBitPosBlock extends BitPosBlock {

  InvBitPosBlock(short[] positions) {
    super(positions);
  }

  @Override
  public Type getType() { return Type.INV_POS_BLOCK; }

  @Override
  public int cardinality() { return BitJynx.BITS_PER_BLOCK - _positions.length; }

  @Override
  public boolean exists(int pos) {
    int result = unsignedBinarySearch(_positions, pos);
    return result < 0;
  }

  @Override
  public int lastBitPos() {
    if (_positions.length == 0)
      return BitJynx.BITS_PER_BLOCK - 1;
    else {
      int lastBitPos = BitJynx.BITS_PER_BLOCK - 1;
      for (int i = _positions[_positions.length - 1]; i >= 0; --i) {
        if (Short.toUnsignedInt(_positions[i]) != lastBitPos)
          return lastBitPos;
        else
          lastBitPos--;
      }
      return lastBitPos;
    }
  }

  private static class Counter {
    private int _cnt = 0;
    Counter(int start) {
      _cnt = start;
    }

    public int get() { return _cnt; }
    public void inc() { ++_cnt; }
  }

  @Override
  public IntStream stream() {
    final Counter idx = new Counter(0);
    return IntStream.range(0, BitJynx.BITS_PER_BLOCK).filter(i -> {
      int pos = Short.toUnsignedInt(_positions[idx.get()]);
      if (pos == i) {
        idx.inc();
        return false;
      }
      else {
        return true;
      }
    });
  }

  @Override
  public IBlock not() {
    return new BitPosBlock(_positions);
  }

  @Override
  public IBlock and(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return EmptyBlock.instance;
      case FULL_BLOCK:
        return new InvBitPosBlock(this._positions);
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        short [] result = andInvLike(b._positions, this._positions);
        if (result.length == 0)
          return FullBlock.instance;
        else
          return new BitPosBlock(result);
      }
      case INV_POS_BLOCK: {
        InvBitPosBlock b = (InvBitPosBlock) v2;
        short[] result = orLike(this._positions, b._positions);
        if (result.length == 0)
          return FullBlock.instance;
        else
          return new InvBitPosBlock(result);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock or(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return new InvBitPosBlock(_positions);
      case FULL_BLOCK:
        return FullBlock.instance;
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        short[] result = orInvLike(b._positions, this._positions);
        if (result.length == 0)
          return FullBlock.instance;
        else
          return new InvBitPosBlock(result);
      }
      case INV_POS_BLOCK: {
        InvBitPosBlock b = (InvBitPosBlock)v2;
        short[] result = andLike(this._positions, b._positions);
        if (result.length == 0)
          return FullBlock.instance;
        else
          return new InvBitPosBlock(result);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("BitPosBlock size ").append(_positions.length).append(": [ ");
    boolean first = true;
    for(short i: _positions) {
      int v = Short.toUnsignedInt(i); // simulating unsigned int for display
      if (first) {
        first = false;
        sb.append(v);
      } else
        sb.append(", ").append(v);
    }
    sb.append(" ]");
    return sb.toString();
  }

}
