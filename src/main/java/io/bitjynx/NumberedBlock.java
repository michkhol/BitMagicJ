package io.bitjynx;

import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;

class NumberedBlock /*implements Comparable<NumberedBlock> */ {
  final int no;
  final IBlock block;

  NumberedBlock(int no, IBlock b) {
    this.no = no;
    this.block = b;
  }

  IBlock andOp(NumberedBlock v2) {
    return block.and(v2.block);
  }

  IBlock orOp(NumberedBlock v2) {
    return block.or(v2.block);
  }

  IBlock xorOp(NumberedBlock v2) {
    return block.xor(v2.block);
  }

  IBlock subOp(NumberedBlock v2) {
    return block.sub(v2.block);
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
