package io.bitjynx;

import scala.Int;

import java.util.Map;

public final class IntVector {

  private final BitJynx[] _numbers;
  private final BitJynx _positions;

  private IntVector(BitJynx p, BitJynx[] a) {
    _positions = p;
    _numbers = a;
  }

  public IntVector(Map<Integer, Integer> map) {
    int[] posArray = map.keySet().stream().mapToInt(Integer::intValue).toArray();
    _positions = new BitJynx(posArray);
    _numbers = new BitJynx[Integer.SIZE];
    int [] buf = new int[map.size()];
    for(int i = 0; i < Integer.SIZE; ++i) {
      int mask = 1 << i;
      int idx = 0;
      for(Map.Entry<Integer, Integer> me : map.entrySet()) {
        if ((me.getValue() & mask) != 0) {
          buf[idx] = me.getKey();
          ++idx;
        }
      }
      _numbers[i] = (idx == 0 ? BitJynx.empty : new BitJynx(buf, idx));
    }
  }

  public IntVector and(int v) {
    BitJynx[] bmAnd = new BitJynx[Integer.SIZE];
    for (int i = 0; i < Integer.SIZE; ++i) {
      int mask = 1 << i;
      if ((v & mask) != 0)
        bmAnd[i] = _numbers[i].and(_positions);
      else
        bmAnd[i] = BitJynx.empty;
    }
    return new IntVector(_positions, bmAnd);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < Integer.SIZE; ++i) {
      sb.append("Bit ").append(i).append(": ").append(_numbers[i].toString()).append("\n");
    }
    return sb.toString();
  }
}
