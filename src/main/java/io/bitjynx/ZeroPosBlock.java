package io.bitjynx;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * This class has zeroes as bit postions
 */
class ZeroPosBlock extends AbstractBitPosBlock {

  ZeroPosBlock(short[] positions) {
    super(positions);
  }

  @Override
  public Type getType() { return Type.ZERO_POS_BLOCK; }

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

  @Override
  public IntStream stream() {
    final Counter idx = new Counter(0); // Using special class to use final for closure
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
      case UNITY_BLOCK:
        return new ZeroPosBlock(this._positions);
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newBitPosBlock(andOnes(b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock) v2;
        return newZeroPosBlock(orLike(this._positions, b._positions));
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock or(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return new ZeroPosBlock(_positions);
      case UNITY_BLOCK:
        return UnityBlock.instance;
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newZeroPosBlock(orOnes(b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock)v2;
        return new ZeroPosBlock(andLike(this._positions, b._positions));
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock xor(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return new ZeroPosBlock(_positions);
      case UNITY_BLOCK:
        return not();
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newZeroPosBlock(xorOnes(b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock)v2;
        return newBitPosBlock(xorLike(this._positions, b._positions));
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock nand(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return UnityBlock.instance;
      case UNITY_BLOCK:
        return not();
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        return newZeroPosBlock(andOnes(b._positions));
      }
      case ZERO_POS_BLOCK: {
        ZeroPosBlock b = (ZeroPosBlock) v2;
        return newBitPosBlock(orLike(this._positions, b._positions));
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (!(o instanceof ZeroPosBlock))
      return false;
    else {
      ZeroPosBlock zb = (ZeroPosBlock)o;
      return this.getType() == zb.getType() && Arrays.equals(this._positions, zb._positions);
    }
  }

/////////////////////////////////////////////////////// Internal implementation ////////////////////////////////////////

  /**
   * Performs AND operation with unity array
   *
   * @param ones unity array
   * @return unity array!!!
   */
  short[] andOnes(short[] ones) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[ones.length];

    while (i < ones.length && j < this._positions.length) {
      if (ones[i] < this._positions[j])
        temp[counter++] = ones[i++];
      else if (this._positions[j] < ones[i])
        j++;
      else {
        i++;
        j++;
      }
    }
    // Add remainder
    while (i < ones.length)
      temp[counter++] = ones[i++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  /**
   * Performs OR operation with unity array
   *
   * @param ones   array of ones
   * @return zero array!!!
   */
  short[] orOnes(short[] ones) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[this._positions.length];

    while (i < ones.length && j < this._positions.length) {
      if (ones[i] < this._positions[j])
        i++;
      else if (this._positions[j] < ones[i])
        temp[counter++] = this._positions[j++];
      else {
        i++;
        j++;
      }
    }
    // Add remainder
    while (j < this._positions.length)
      temp[counter++] = this._positions[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }


  /**
   * Performs XOR operation with unity array
   *
   * @param ones array of ones
   * @return zero array!!!
   */
  short[] xorOnes(short[] ones) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(ones.length + this._positions.length, BitJynx.BITS_PER_BLOCK)];

    while (i < ones.length && j < this._positions.length) {
      if (ones[i] < this._positions[j])
        temp[counter++] = ones[i++];
      else if (this._positions[j] < ones[i])
        temp[counter++] = this._positions[j++];
      else {
        i++;
        j++;
      }
    }
    // Add remainders
    while (i < ones.length)
      temp[counter++] = ones[i++];
    while (j < this._positions.length)
      temp[counter++] = this._positions[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

}
