package io.bitjynx;

interface IBlock {
  public long size();
  public int cardinality();

  /**
   * Checks if a bit set at specified position
   *
   * @param pos integer to cover the 'unsigned short' range
   * @return true if bit is set
   */
  public boolean exists(int pos);
}
