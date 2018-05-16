package io.bitjynx;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Must not contain BitPosBlocks, only ZeroBitPosBlocks
 */
public final class ZeroVector extends AbstractVector {

  /**
   * For internal use only!!!
   * @param blocks
   */
  ZeroVector(ArrayList<NumberedBlock> blocks) {
    super(blocks/*, Type.ZERO*/);
  }

  /**
   * Cardinality does not make much sence here
   *
   * @return -1
   */
  @Override
  public int cardinality() {
      return -1;
  }

  /**
   * Highest bit position is undefined
   *
   * @return -1
   */
  @Override
  public int getMaxBitPosition() {
    return -1;
  }

  /**
   * Returns the bit value at the specified position
   *
   * @param idx bit position
   * @return true or false
   */
  @Override
  public boolean get(int idx) {
    // find the block
    int blockIdx = idx >> BIT_BLOCK_POWER;
    NumberedBlock found = indexedBinarySearch(_blockArray, blockIdx);
    if (found == null) return true;
    else {
      int pos = idx - (blockIdx << BIT_BLOCK_POWER);
      return !found.block.exists(pos);
    }
  }

  @Override
  public AbstractVector not() {
    ArrayList<NumberedBlock> al = new ArrayList<>(this._blockArray.size());
    for(NumberedBlock nb : this._blockArray) {
      al.add(new NumberedBlock(nb.no, nb.block.not()));
    }
    return new UnityVector(al);
  }

  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector and(AbstractVector v) {
    return v.andOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new UnityVector(v.intersect(this._blockArray));
//      case ZERO:
//        return new ZeroVector(union(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractVector type");
//    }
  }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector or(AbstractVector v) {
    return v.orOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new ZeroVector(v.unionZero(this._blockArray));
//      case ZERO:
//        return new ZeroVector(intersect(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractVector type");
//    }
  }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector xor(AbstractVector v) {
    return v.xorOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new ZeroVector(exDisjunction(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractVector type");
//    }
  }

  /**
   * Performs NAND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector nand(AbstractVector v) {
    return v.nandOp(this);
  }

  /**
   * Performs SUB operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public AbstractVector sub(AbstractVector v) {
    return v.subOp(this);
  }

  /**
   * Optimizes vector storage, may be slow.
   */
  @Override
  public AbstractVector optimize() {
    // TODO: Optimize block storage
    return new ZeroVector(_blockArray);
  }

  @Override
  public IntStream stream() {
    throw new RuntimeException("Infinite number of bits, use stream(int limit).");
  }

  @Override
  public IntStream stream(int limit) {
    final Counter idx = new Counter(0); // Using special class to use final for closure
    int floor = limit >> BitJynx.BIT_BLOCK_POWER;
    int remainder = limit & (BitJynx.BITS_PER_BLOCK - 1);
    int blocksToStream = remainder > 0 ? floor + 1 : floor;
    return IntStream.range(0, blocksToStream).flatMap(blockNo -> {
      NumberedBlock nb = _blockArray.get(idx.get());
      final int offset =  blockNo << BitJynx.BIT_BLOCK_POWER;
      if (blockNo != nb.no) {
        return IntStream.range(offset, blockNo == blocksToStream - 1 ? limit : offset + BitJynx.BITS_PER_BLOCK);
      }
      else {
        idx.inc();
        IntStream result = nb.block.stream().map(x -> offset + x);
        if (blockNo < blocksToStream - 1)
          return result;
        else
          return result.filter(x -> x < limit);
      }
    });
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cardinality: ").append(cardinality());
    sb.append(", highest bit: ").append(getMaxBitPosition());
    if (_blockArray != null) {
      final AtomicLong counter = new AtomicLong();
      sb.append(", blocks: ").append(_blockArray.size());//.append("\n");
//      _blockMap.forEach((k, v) -> { /*sb.append(k).append(": ").append(v.toString()).append("\n"); */counter.addAndGet(v.size()); });
//      sb.append("Total block size: ").append(counter.getAsIntArray());
    }
    return sb.toString();
  }

  @Override
  protected AbstractVector andOp(UnityVector v) {
    return new UnityVector(intersectZero(v._blockArray, this._blockArray));
  }

  @Override
  protected AbstractVector andOp(ZeroVector v) {
    return new ZeroVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector orOp(UnityVector v) {
    return new ZeroVector(unionZero(v._blockArray, this._blockArray));
  }

  @Override
  protected AbstractVector orOp(ZeroVector v) {
    return new ZeroVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector xorOp(UnityVector v) {
    return new ZeroVector(exDisjunctionZero(v._blockArray, this._blockArray));
  }

  @Override
  protected AbstractVector xorOp(ZeroVector v) {
    return new UnityVector(exDisjunction(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector nandOp(UnityVector v) {
    return new ZeroVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector nandOp(ZeroVector v) {
    return new UnityVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector subOp(UnityVector v) {
    return new ZeroVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected AbstractVector subOp(ZeroVector v) {
    return new UnityVector(intersectZero(v._blockArray, this._blockArray));
  }
}
