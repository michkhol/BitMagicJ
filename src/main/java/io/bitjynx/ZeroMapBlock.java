package io.bitjynx;

import java.util.Arrays;
import java.util.stream.IntStream;

class ZeroMapBlock extends AbstractMapBlock {

  ZeroMapBlock(long[] map) {
    super(map);
  }

  @Override
  public Type getType() {
    return Type.ZERO_MAP_BLOCK;
  }

  @Override
  public int cardinality() {
    int sum = 0;
    for(long l: _map) { sum += Long.SIZE - Long.bitCount(l); }
    return sum;
  }

  @Override
  public boolean exists(int pos) {
    int cellNo = pos >> CELL_POWER;
    int offset = pos & CELL_BIT_MASK;
    long result = _map[cellNo] & (HIGH_MASK >>> offset);
    return result == 0;
  }

  // TODO: Implement
  @Override
  public int lastBitPos() {
    // Linear search
    int i = _map.length - 1;
    for( ; i >= 0; --i)
      if (Long.bitCount(_map[i]) != 0)
        break;
    return i < 0 ? 0 : (i << CELL_POWER) + 63 - Long.numberOfTrailingZeros(_map[i]);
  }

  // TODO: Implement
  @Override
  public IntStream stream() {
    return IntStream.range(0, BitJynx.BITS_PER_BLOCK).filter(this::exists);
  }

  @Override
  public IBlock not() {
    return new BitMapBlock(_map);
  }

  protected IBlock and(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return EmptyBlock.instance;
      case UNITY_BLOCK:
        return new ZeroMapBlock(this._map);
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        long acc = 0;
        for(int i = 0; i < _map.length; i += 1) {
          map[i] = _map[i] | ~b._map[i];
          acc &= map[i];
        }
        return acc == -1L ? EmptyBlock.instance : new ZeroMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        long acc = 0;
        for(int i = 0; i < _map.length; i += 1) {
          map[i] = _map[i] | b._map[i];
          acc &= map[i];
        }
        return acc == -1L ? EmptyBlock.instance : new ZeroMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  protected IBlock or(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return new ZeroMapBlock(_map);
      case UNITY_BLOCK:
        return UnityBlock.instance;
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] & ~b._map[i];
        }
        return new ZeroMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] & b._map[i];
        }
        return new ZeroMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  protected IBlock xor(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return new ZeroMapBlock(_map);
      case UNITY_BLOCK:
        return not();
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = ~(~_map[i] ^ b._map[i]);
        }
        return new ZeroMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = ~(~_map[i] ^ ~b._map[i]);
        }
        return new ZeroMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  protected IBlock nand(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return UnityBlock.instance;
      case UNITY_BLOCK:
        return not();
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = ~(_map[i] | ~b._map[i]);
        }
        return new ZeroMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = ~(_map[i] | b._map[i]);
        }
        return new ZeroMapBlock(map);
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
    else if (!(o instanceof ZeroMapBlock))
      return false;
    else {
      ZeroMapBlock b = (ZeroMapBlock)o;
      return this.getType() == b.getType() && Arrays.equals(_map, b._map);
    }
  }
}