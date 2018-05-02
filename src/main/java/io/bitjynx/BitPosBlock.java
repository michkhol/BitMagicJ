package io.bitjynx;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

class BitPosBlock implements IBlock {


  // Used as 'unsigned short'
  private short[] _positions;

  BitPosBlock(short[] positions) {
    this._positions = positions;
  }

  // Taken from java.util.Arrays, adapted for 'unsigned short'
  private static int unsignedBinarySearch(short[] a, int key) {
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
  public BlockType getType() { return BlockType.POS_BLOCK; }

  @Override
  public long size() { return _positions.length; }

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

  static IBlock and(IBlock v1, IBlock v2) {
    BitPosBlock vv1 = (BitPosBlock)v1;
    switch(v2.getType()) {
      case FULL_BLOCK:
        return new BitPosBlock(vv1._positions);
      case POS_BLOCK:
        BitPosBlock b = (BitPosBlock) v2;
        int i = 0, j = 0, counter = 0;
        short[] temp = new short[Integer.min(vv1._positions.length, b._positions.length)];

        while (i < vv1._positions.length && j < b._positions.length) {
          if (vv1._positions[i] < b._positions[j])
            i++;
          else if (b._positions[j] < vv1._positions[i])
            j++;
          else {
            temp[counter++] = vv1._positions[i];
            i++;
            j++;
          }
        }
        if (counter == 0)
          return null;
        else {
          short[] result = new short[counter];
          System.arraycopy(temp, 0, result, 0, counter);
          return new BitPosBlock(result);
        }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  static IBlock or(IBlock v1, IBlock v2) {
    BitPosBlock vv1 = (BitPosBlock)v1;
    switch(v2.getType()) {
      case FULL_BLOCK:
        return FullBlock.instance;
      case POS_BLOCK:
        BitPosBlock b = (BitPosBlock)v2;
        int i = 0, j = 0, counter = 0;
        short[] temp = new short[Integer.min(vv1._positions.length + b._positions.length, BitJynx.BITS_PER_BLOCK)];

        while (i < vv1._positions.length && j < b._positions.length) {
          if (vv1._positions[i] < b._positions[j]) {
            temp[counter++] = vv1._positions[i++];
          }
          else if (b._positions[j] < vv1._positions[i]) {
            temp[counter++] = b._positions[j++];
          }
          else {
            temp[counter++] = vv1._positions[i++];
            j++;
          }
        }
        // Add remaining elements
        while(i < vv1._positions.length)
          temp[counter++] = vv1._positions[i++];
        while(j < b._positions.length)
          temp[counter++] = b._positions[j++];

        if (counter == 0)
          return null;
        else {
          short[] result = new short[counter];
          System.arraycopy(temp, 0, result, 0, counter);
          return new BitPosBlock(result);
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
