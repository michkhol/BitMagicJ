package io.bitjynx;

class NumberedBlock implements Comparable<NumberedBlock> {
  final int no;
  final IBlock block;

  NumberedBlock(int no, IBlock b) {
    this.no = no;
    this.block = b;
  }

  @Override
  public int hashCode() { return no; }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof NumberedBlock))
      return false;
    return ((NumberedBlock)obj).no == this.no;
  }

  @Override
  public int compareTo(NumberedBlock nb) {
    return this.no - nb.no;
  }

}
