package io.bitjynx;

import java.util.stream.IntStream;

class BitPosBlock implements IBlock {

  // Used as 'unsigned short'
  protected short[] _positions;

  BitPosBlock(short[] positions) {
    this._positions = positions;
  }

  // Taken from java.util.Arrays, adapted for 'unsigned short'
  protected static int unsignedBinarySearch(short[] a, int key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      short midVal = a[mid];

      int intMidVal = Short.toUnsignedInt(midVal);
      if (intMidVal < key)
        low = mid + 1;
      else if (intMidVal > key)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1);  // key not found.
  }

  @Override
  public Type getType() { return Type.POS_BLOCK; }

  @Override
  public int cardinality() { return _positions.length; }

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
    return new InvBitPosBlock(_positions);
  }

  @Override
  public IBlock and(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return EmptyBlock.instance;
      case FULL_BLOCK:
        return new BitPosBlock(this._positions);
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        short[] result = andLike(this._positions, b._positions);
        if (result.length == 0)
          return EmptyBlock.instance;
        else
          return new BitPosBlock(result);
      }
      case INV_POS_BLOCK: {
        InvBitPosBlock b = (InvBitPosBlock) v2;
        short[] result = andInvLike(this._positions, b._positions);
        if (result.length == 0)
          return EmptyBlock.instance;
        else
          return new BitPosBlock(result);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock or(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return new BitPosBlock(_positions);
      case FULL_BLOCK:
        return FullBlock.instance;
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        short[] result = orLike(this._positions, b._positions);
        if (result.length == 0)
          return EmptyBlock.instance;
        else
          return new BitPosBlock(result);
      }
      case INV_POS_BLOCK: {
        InvBitPosBlock b = (InvBitPosBlock) v2;
        short[] result = orInvLike(this._positions, b._positions);
        if (result.length == 0)
          return EmptyBlock.instance;
        else
          return new InvBitPosBlock(result);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock xor(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return new BitPosBlock(_positions);
      case FULL_BLOCK:
        return v2.not();
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        short[] result = xorLike(this._positions, b._positions);
        if (result.length == 0)
          return EmptyBlock.instance;
        else
          return new BitPosBlock(result);
      }
      case INV_POS_BLOCK: {
        InvBitPosBlock b = (InvBitPosBlock) v2;
        short[] result = xorInvLike(this._positions, b._positions);
        if (result.length == 0)
          return EmptyBlock.instance;
        else
          return new InvBitPosBlock(result);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock sub(IBlock v2) {
    switch(v2.getType()) {
      case EMPTY_BLOCK:
        return FullBlock.instance;
      case FULL_BLOCK:
        return this.not();
      case POS_BLOCK: {
        BitPosBlock b = (BitPosBlock) v2;
        short[] result = andLike(this._positions, b._positions);
        if (result.length == 0)
          return FullBlock.instance;
        else
          return new InvBitPosBlock(result);
      }
      case INV_POS_BLOCK: {
        InvBitPosBlock b = (InvBitPosBlock) v2;
        short[] result = andInvLike(this._positions, b._positions);
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

  protected short[] andLike(short[] positions1, short[] positions2) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(positions1.length, positions2.length)];

    while (i < positions1.length && j < positions2.length) {
      if (positions1[i] < positions2[j])
        i++;
      else if (positions2[j] < positions1[i])
        j++;
      else {
        temp[counter++] = positions1[i];
        i++;
        j++;
      }
    }
    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  protected short[] orLike(short[] positions1, short[] positions2) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(positions1.length + positions2.length, BitJynx.BITS_PER_BLOCK)];

    while (i < positions1.length && j < positions2.length) {
      if (positions1[i] < positions2[j]) {
        temp[counter++] = positions1[i++];
      }
      else if (positions2[j] < positions1[i]) {
        temp[counter++] = positions2[j++];
      }
      else {
        temp[counter++] = positions1[i++];
        j++;
      }
    }
    // Add remaining elements
    while(i < positions1.length)
      temp[counter++] = positions1[i++];
    while(j < positions2.length)
      temp[counter++] = positions2[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  protected short[] xorLike(short[] positions1, short[] positions2) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(positions1.length + positions2.length, BitJynx.BITS_PER_BLOCK)];

    while (i < positions1.length && j < positions2.length) {
      if (positions1[i] < positions2[j]) {
        temp[counter++] = positions1[i++];
      }
      else if (positions2[j] < positions1[i]) {
        temp[counter++] = positions2[j++];
      }
      else {
//        temp[counter++] = positions1[i++];
        j++;
      }
    }
    // Add remaining elements
    while(i < positions1.length)
      temp[counter++] = positions1[i++];
    while(j < positions2.length)
      temp[counter++] = positions2[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  protected short[] andInvLike(short[] ones, short[] zeroes) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[ones.length];

    while (i < ones.length && j < zeroes.length) {
      if (ones[i] < zeroes[j])
        temp[counter++] = ones[i++];
      else if (zeroes[j] < ones[i])
        j++;
      else {
        i++;
        j++;
      }
    }
    // Add remainder
    while(i < ones.length)
      temp[counter++] = ones[i++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  /**
   * Returns array of zeroes!
   *
   * @param ones array of ones
   * @param zeroes array of zeroes
   * @return 'inverted' array
   */
  protected short[] orInvLike(short[] ones, short[] zeroes) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[zeroes.length];

    while (i < ones.length && j < zeroes.length) {
      if (ones[i] < zeroes[j])
        i++;
      else if (zeroes[j] < ones[i])
        temp[counter++] = zeroes[j++];
      else {
        i++;
        j++;
      }
    }
    // Add remainder
    while(j < zeroes.length)
      temp[counter++] = zeroes[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  /**
   * Returns array of zeroes!
   *
   * @param ones array of ones
   * @param zeroes array of zeroes
   * @return 'inverted' array
   */
  protected short[] xorInvLike(short[] ones, short[] zeroes) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(ones.length + zeroes.length, BitJynx.BITS_PER_BLOCK)];

    while (i < ones.length && j < zeroes.length) {
      if (ones[i] < zeroes[j])
        temp[counter++] = ones[i++];
      else if (zeroes[j] < ones[i])
        temp[counter++] = zeroes[j++];
      else {
        i++;
        j++;
      }
    }
    // Add remainders
    while(i < ones.length)
      temp[counter++] = ones[i++];
    while(j < zeroes.length)
      temp[counter++] = zeroes[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  //  static IBlock and(IBlock v1, IBlock v2) {
//    BitPosBlock vv1 = (BitPosBlock)v1;
//    switch(v2.getType()) {
//      case FULL_BLOCK:
//        return new BitPosBlock(vv1._positions);
//      case POS_BLOCK:
//        BitPosBlock b = (BitPosBlock) v2;
//        int i = 0, j = 0, counter = 0;
//        short[] temp = new short[Integer.min(vv1._positions.length, b._positions.length)];
//
//        while (i < vv1._positions.length && j < b._positions.length) {
//          if (vv1._positions[i] < b._positions[j])
//            i++;
//          else if (b._positions[j] < vv1._positions[i])
//            j++;
//          else {
//            temp[counter++] = vv1._positions[i];
//            i++;
//            j++;
//          }
//        }
//        if (counter == 0)
//          return null;
//        else {
//          short[] result = new short[counter];
//          System.arraycopy(temp, 0, result, 0, counter);
//          return new BitPosBlock(result);
//        }
//      default:
//        throw new RuntimeException("Unsupported block type");
//    }
//  }
//
//  static IBlock or(IBlock v1, IBlock v2) {
//    BitPosBlock vv1 = (BitPosBlock)v1;
//    switch(v2.getType()) {
//      case FULL_BLOCK:
//        return FullBlock.instance;
//      case POS_BLOCK:
//        BitPosBlock b = (BitPosBlock)v2;
//        int i = 0, j = 0, counter = 0;
//        short[] temp = new short[Integer.min(vv1._positions.length + b._positions.length, BitJynx.BITS_PER_BLOCK)];
//
//        while (i < vv1._positions.length && j < b._positions.length) {
//          if (vv1._positions[i] < b._positions[j]) {
//            temp[counter++] = vv1._positions[i++];
//          }
//          else if (b._positions[j] < vv1._positions[i]) {
//            temp[counter++] = b._positions[j++];
//          }
//          else {
//            temp[counter++] = vv1._positions[i++];
//            j++;
//          }
//        }
//        // Add remaining elements
//        while(i < vv1._positions.length)
//          temp[counter++] = vv1._positions[i++];
//        while(j < b._positions.length)
//          temp[counter++] = b._positions[j++];
//
//        if (counter == 0)
//          return null;
//        else {
//          short[] result = new short[counter];
//          System.arraycopy(temp, 0, result, 0, counter);
//          return new BitPosBlock(result);
//        }
//      default:
//        throw new RuntimeException("Unsupported block type");
//    }
//  }
}
