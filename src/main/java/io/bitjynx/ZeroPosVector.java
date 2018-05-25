package io.bitjynx;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Must not contain BitPosBlocks, only ZeroBitPosBlocks
 */
final class ZeroPosVector extends AbstractPosVector {

  /**
   * For internal use only!!!
   * @param blocks
   */
  ZeroPosVector(ArrayList<NumberedBlock> blocks) {
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
  public AbstractPosVector not() {
    ArrayList<NumberedBlock> al = new ArrayList<>(this._blockArray.size());
    for(NumberedBlock nb : this._blockArray) {
      al.add(new NumberedBlock(nb.no, nb.block.not()));
    }
    return new UnityPosVector(al);
  }

  /**
   * Performs AND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector and(IVector v) {
    return ((AbstractPosVector)v).andOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new UnityPosVector(v.intersect(this._blockArray));
//      case ZERO:
//        return new ZeroPosVector(union(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractPosVector type");
//    }
  }

  /**
   * Performs OR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector or(IVector v) {
    return ((AbstractPosVector)v).orOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new ZeroPosVector(v.unionZero(this._blockArray));
//      case ZERO:
//        return new ZeroPosVector(intersect(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractPosVector type");
//    }
  }

  /**
   * Performs XOR operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector xor(IVector v) {
    return ((AbstractPosVector)v).xorOp(this);
//    switch (v.getType()) {
//      case ONE:
//        return new ZeroPosVector(exDisjunction(v._blockArray));
//      default:
//        throw new RuntimeException("Invalid AbstractPosVector type");
//    }
  }

  /**
   * Performs NAND operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector nand(IVector v) {
    return ((AbstractPosVector)v).nandOp(this);
  }

  /**
   * Performs SUB operation and returns a new vector
   *
   * @param v operand
   * @return a new bit vector
   */
  @Override
  public IVector sub(IVector v) {
    return ((AbstractPosVector)v).subOp(this);
  }

  /**
   * Optimizes vector storage, may be slow.
   */
  @Override
  public IVector optimize() {
    // TODO: Optimize block storage
    return new ZeroPosVector(_blockArray);
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
  protected IVector andOp(UnityPosVector v) {
    return new UnityPosVector(intersectZero(v._blockArray, this._blockArray));
  }

  @Override
  protected IVector andOp(ZeroPosVector v) {
    return new ZeroPosVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector orOp(UnityPosVector v) {
    return new ZeroPosVector(unionZero(v._blockArray, this._blockArray));
  }

  @Override
  protected IVector orOp(ZeroPosVector v) {
    return new ZeroPosVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector xorOp(UnityPosVector v) {
    return new ZeroPosVector(exDisjunctionZero(v._blockArray, this._blockArray));
  }

  @Override
  protected IVector xorOp(ZeroPosVector v) {
    return new UnityPosVector(exDisjunction(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector nandOp(UnityPosVector v) {
    return new ZeroPosVector(intersect(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector nandOp(ZeroPosVector v) {
    return new UnityPosVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector subOp(UnityPosVector v) {
    return new ZeroPosVector(union(this._blockArray, v._blockArray));
  }

  @Override
  protected IVector subOp(ZeroPosVector v) {
    return new UnityPosVector(intersectZero(v._blockArray, this._blockArray));
  }
}
