package io.bitjynx;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

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

  static class AndTask extends RecursiveTask<Map.Entry<Long, IBlock>> {
    private final Long _key;
    private final BitPosBlock _v1;
    private final BitPosBlock _v2;

    AndTask(Long key, BitPosBlock v1, BitPosBlock v2) {
      _key = key;
      _v1 = v1;
      _v2 = v2;
    }

    @Override
    protected Map.Entry<Long, IBlock> compute() {
      IBlock b = _v1.and(_v2);
      return new AbstractMap.SimpleEntry<>(_key, b);
    }
  }

  private BitPosBlock and(BitPosBlock b) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(this._positions.length, b._positions.length)];

    while (i < this._positions.length && j < b._positions.length) {
      if (this._positions[i] < b._positions[j])
        i++;
      else if (b._positions[j] < this._positions[i])
        j++;
      else {
        temp[counter++] = this._positions[i];
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
  }

  public RecursiveTask<Map.Entry<Long, IBlock>> andTask(Long key, IBlock right) {
    return new AndTask(key, this, (BitPosBlock)right);
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
