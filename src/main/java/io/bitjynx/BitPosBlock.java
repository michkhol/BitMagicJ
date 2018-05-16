package io.bitjynx;

import java.util.Arrays;
import java.util.stream.IntStream;

class BitPosBlock extends AbstractBitPosBlock {


  BitPosBlock(short[] positions) {
    super(positions);
  }

  @Override
  public Type getType() {
    return Type.POS_BLOCK;
  }

  @Override
  public int cardinality() {
    return _positions.length;
  }

  @Override
  public boolean exists(int pos) {
    int result = unsignedBinarySearch(_positions, pos);
    return result >= 0;
  }

  @Override
  public int lastBitPos() {
    return Short.toUnsignedInt(_positions[_positions.length - 1]);
  }

  @Override
  public IntStream stream() {
    return IntStream.range(0, _positions.length).map(i -> Short.toUnsignedInt(_positions[i]));
  }

  @Override
  public IBlock not() {
    return new ZeroPosBlock(_positions);
  }

  @Override
  public IBlock and(IBlock v2) {
    switch (v2.getType()) {
      case EMPTY_BLOCK:
        return EmptyBlock.instance;
      case UNITY_BLOCK:
        return new BitPosBlock(this._positions);
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newBitPosBlock(andLike(this._positions, b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock) v2;
        return newBitPosBlock(b.andOnes(this._positions));
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock or(IBlock v2) {
    switch (v2.getType()) {
      case EMPTY_BLOCK:
        return new BitPosBlock(_positions);
      case UNITY_BLOCK:
        return UnityBlock.instance;
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newBitPosBlock(orLike(this._positions, b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock) v2;
        return newZeroPosBlock(b.orOnes(this._positions));
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock xor(IBlock v2) {
    switch (v2.getType()) {
      case EMPTY_BLOCK:
        return new BitPosBlock(this._positions);
      case UNITY_BLOCK:
        return newZeroPosBlock(this._positions);
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newBitPosBlock(xorLike(this._positions, b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock) v2;
        return newZeroPosBlock(b.xorOnes(this._positions));
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock nand(IBlock v2) {
    switch (v2.getType()) {
      case EMPTY_BLOCK:
        return UnityBlock.instance;
      case UNITY_BLOCK:
        return newZeroPosBlock(this._positions);
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newZeroPosBlock(andLike(this._positions, b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock) v2;
        return newZeroPosBlock(b.andOnes(this._positions));
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
    for (short i : _positions) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (!(o instanceof BitPosBlock))
      return false;
    else {
      BitPosBlock b = (BitPosBlock)o;
      return this.getType() == b.getType() && Arrays.equals(this._positions, b._positions);
    }
  }


  //////////////////////////////////////////////// Internal implementation ///////////////////////////////////////

//  private IBlock andOp(UnityBlock b) { return new BitPosBlock(_positions); }
//  private IBlock andOp(EmptyBlock b) { return EmptyBlock.instance; }
//  private IBlock andOp(BitPosBlock b) { return newBitPosBlock(intersect(this._positions, b._positions)); }
//  private IBlock andOp(ZeroPosBlock b) { return newBitPosBlock(b.andOnes(this._positions)); }
}