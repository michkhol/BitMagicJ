package io.bitjynx;

import java.util.concurrent.RecursiveTask;

class NumberedBlock /*implements Comparable<NumberedBlock> */ {
  final int no;
  final IBlock block;

  NumberedBlock(int no, IBlock b) {
    this.no = no;
    this.block = b;
  }

  public RecursiveTask<NumberedBlock> andTask(int key, NumberedBlock right) {
    switch(block.getType()) {
      case POS_BLOCK:
        return new OpTask(key, block, right.block, BitPosBlock::and);
      default:
        throw new RuntimeException("Insupported block type");
    }
  }

  public RecursiveTask<NumberedBlock> orTask(int key, NumberedBlock right) {
    switch(block.getType()) {
      case POS_BLOCK:
        return new OpTask(key, block, right.block, BitPosBlock::or);
      default:
        throw new RuntimeException("Insupported block type");
    }
  }

//  @Override
//  public int hashCode() { return no; }
//
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj)
//      return true;
//    if (!(obj instanceof NumberedBlock))
//      return false;
//    return ((NumberedBlock)obj).no == this.no;
//  }
//
//  @Override
//  public int compareTo(NumberedBlock nb) {
//    return this.no - nb.no;
//  }

}
