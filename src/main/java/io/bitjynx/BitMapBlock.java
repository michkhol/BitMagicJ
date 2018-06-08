package io.bitjynx;

import java.util.Arrays;
import java.util.stream.IntStream;

class BitMapBlock extends AbstractMapBlock {

  BitMapBlock(int[] positions, int len) {
    super(createBlock(positions, len));
  }

  BitMapBlock(int[] positions) {
    this(positions, positions.length);
  }

  BitMapBlock(long[] map) {
    super(map);
  }

  @Override
  public Type getType() {
    return Type.BIT_MAP_BLOCK;
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
    int offset = pos & CELL_BIT_MASK;
    long result = _map[cellNo] & (HIGH_MASK >>> offset);
    return result != 0;
  }

  @Override
  public int lastBitPos() {
    // Linear search
    int i = _map.length - 1;
    for( ; i >= 0; --i)
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
    return new ZeroMapBlock(_map);
  }

  protected IBlock and(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return EmptyBlock.instance;
      case UNITY_BLOCK:
        return new BitMapBlock(this._map);
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        long acc = 0;
        for(int i = 0; i < _map.length; i += 1) {
          map[i] = _map[i] & b._map[i];
          acc |= map[i];
        }
        return acc == 0 ? EmptyBlock.instance : new BitMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        long acc = 0;
        for(int i = 0; i < _map.length; i += 1) {
          map[i] = _map[i] & ~b._map[i];
          acc |= map[i];
        }
        return acc == 0 ? EmptyBlock.instance : new BitMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  public IBlock and2(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return EmptyBlock.instance;
      case UNITY_BLOCK:
        return new BitMapBlock(this._map);
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        intersect0(this._map, b._map, map);
        return new BitMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  protected IBlock or(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return new BitMapBlock(_map);
      case UNITY_BLOCK:
        return UnityBlock.instance;
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        long acc = 0;
        for(int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] | b._map[i];
          acc &= map[i];
        }
        return acc == -1L ? UnityBlock.instance : new BitMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        long acc = 0;
        for (int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] | ~b._map[i];
          acc &= map[i];
        }
        return acc == -1L ? UnityBlock.instance : new BitMapBlock(map);
      }
      default:
        throw new RuntimeException("Unsupported block type");
    }
  }

  protected IBlock xor(AbstractMapBlock b) {
    switch (b.getType()) {
      case EMPTY_BLOCK:
        return new BitMapBlock(_map);
      case UNITY_BLOCK:
        return not();
      case BIT_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] ^ b._map[i];
        }
        return new BitMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        for(int i = 0; i < _map.length; ++i) {
          map[i] = _map[i] ^ ~b._map[i];
        }
        return new BitMapBlock(map);
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
        long acc = 0;
        for(int i = 0; i < _map.length; ++i) {
          map[i] = ~(_map[i] & b._map[i]);
          acc &= map[i];
        }
        return acc == -1L ? UnityBlock.instance : new BitMapBlock(map);
      }
      case ZERO_MAP_BLOCK: {
        long[] map = new long[_map.length];
        long acc = 0;
        for(int i = 0; i < _map.length; ++i) {
          map[i] = ~_map[i] | b._map[i];
          acc &= map[i];
        }
        return acc == -1L ? UnityBlock.instance : new BitMapBlock(map);
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

//  static void setBit(long[] map, int pos) {
//    int cellNo = pos >> CELL_POWER;
//    int offset = pos & CELL_BIT_MASK;
//    map[cellNo] |= (HIGH_MASK >>> offset);
//  }

  private final static int STEP_POWER = 2;
  private final static int STEP = 1 << STEP_POWER;
  private final static int STEP_MASK = STEP - 1;
  static long[] createBlock(int[] offsets, int len) {
    long[] cells = new long[BitJynx.BITS_PER_BLOCK >> CELL_POWER];
    int endStep = len & ~STEP_MASK;
    int o1, o2, o3, o4;
    for(int i = 0; i < endStep; i += STEP) {
      o1 = offsets[i];
      cells[o1 >> CELL_POWER] |= (HIGH_MASK >>> (o1 & CELL_BIT_MASK));
      o2 = offsets[i+1];
      cells[o2 >> CELL_POWER] |= (HIGH_MASK >>> (o2 & CELL_BIT_MASK));
      o3 = offsets[i+2];
      cells[o3 >> CELL_POWER] |= (HIGH_MASK >>> (o3 & CELL_BIT_MASK));
      o4 = offsets[i+3];
      cells[o4 >> CELL_POWER] |= (HIGH_MASK >>> (o4 & CELL_BIT_MASK));
    }
    // Remainder
    for(int j = endStep; j < len; ++j) {
      cells[offsets[j] >> CELL_POWER] |= (HIGH_MASK >>> (offsets[j] & CELL_BIT_MASK));
    }
    return cells;
  }
}