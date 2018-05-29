package io.bitjynx;

import java.util.Arrays;
import java.util.stream.IntStream;

class BitMapBlock implements IBlock {

  final static int CELL_POWER = 6;
  final static long HIGH_MASK = 0x8000000000000000L;

  // The size is always BLOCK_SIZE / sizeof(long)
  final private long[] _map;

  BitMapBlock(int[] positions) {
    _map = new long[BitJynx.BITS_PER_BLOCK >> CELL_POWER];
    for(int i: positions) {
      setBit(_map, i);
    }
  }

  BitMapBlock(long[] map) {
    _map = map;
  }

  @Override
  public Type getType() {
    return Type.MAP_BLOCK;
  }

  @Override
  public int cardinality() {
    int sum = 0;
    for(long l: _map) { sum += Long.bitCount(l); }
    return sum;
  }

  @Override
  public boolean exists(int pos) {
    int cellNo = pos >> CELL_POWER;
    int offset = pos - (cellNo << CELL_POWER);
    long result = _map[cellNo] & (HIGH_MASK >>> offset);
    return result != 0;
  }

  @Override
  public int lastBitPos() {
    // Linear search
    int i = _map.length - 1;
    for( ; i >=0; --i)
      if (Long.bitCount(_map[i]) != 0)
        break;
    return i < 0 ? 0 : (i << CELL_POWER) + 63 - Long.numberOfTrailingZeros(_map[i]);
  }

  @Override
  public IntStream stream() {
    return IntStream.range(0, BitJynx.BITS_PER_BLOCK).filter(this::exists);
  }

  @Override
  public IBlock not() {
    long[] map = new long[_map.length];
    for(int i = 0; i < _map.length; ++i) {
      map[i] = ~_map[i];
    }
    return new BitMapBlock(map);
  }

  @Override
  public IBlock and(IBlock v2) {
    switch (v2.getType()) {
      case EMPTY_BLOCK:
        return EmptyBlock.instance;
      case UNITY_BLOCK:
        return new BitMapBlock(this._map);
      case MAP_BLOCK: {
        BitMapBlock b = (BitMapBlock) v2;
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; i = i + 1) {
          map[i] = _map[i] & b._map[i];
//          map[i+1] = _map[i+1] & b._map[i+1];
//          map[i+2] = _map[i+2] & b._map[i+2];
//          map[i+3] = _map[i+3] & b._map[i+3];
        }
        return new BitMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock or(IBlock v2) {
    switch (v2.getType()) {
      case EMPTY_BLOCK:
        return new BitMapBlock(_map);
      case UNITY_BLOCK:
        return UnityBlock.instance;
      case MAP_BLOCK: {
        BitMapBlock b = (BitMapBlock) v2;
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] | b._map[i];
        }
        return new BitMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public IBlock xor(IBlock v2) {
    switch (v2.getType()) {
      case EMPTY_BLOCK:
        return new BitMapBlock(_map);
      case UNITY_BLOCK:
        return not();
      case MAP_BLOCK: {
        BitMapBlock b = (BitMapBlock) v2;
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] ^ b._map[i];
        }
        return new BitMapBlock(map);
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
        return not();
      case MAP_BLOCK: {
        BitMapBlock b = (BitMapBlock) v2;
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = ~(_map[i] & b._map[i]);
        }
        return new BitMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(cardinality());
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_map);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (!(o instanceof BitMapBlock))
      return false;
    else {
      BitMapBlock b = (BitMapBlock)o;
      return this.getType() == b.getType() && Arrays.equals(_map, b._map);
    }
  }

  static void setBit(long[] map, int pos) {
    int cellNo = pos >> CELL_POWER;
    int offset = pos - (cellNo << CELL_POWER);
    map[cellNo] |= (HIGH_MASK >>> offset);
  }
}