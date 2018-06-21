package io.bitmagic;

import io.bitmagic.core.Strategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class BitVectorTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void version() {
    String version = BitVector.getVersion();
    System.out.println("Bitmagic version " + version);
  }

  @Test
  public void size() {
    int bSize = 10000;
    int bSize2 = 13445;
    BitVector bv = new BitVector(bSize, Strategy.BM_BIT);
    long sz = bv.getSize();
    System.out.println("BitVector size: " + sz);
    assertEquals(sz, bSize);
    bv.setSize(bSize2);
    assertEquals(bv.getSize(), bSize2);
  }

  @Test
  public void serialization() {
    BitVector bv = new BitVector();
    boolean bit = true;
    long limit = 1000000L;
    for(long idx = 0; idx < limit; idx++) {
      bit = !bit;
      bv.set(idx, bit);
    }
    System.out.println("Bit count1: " + bv.count());
    byte[] serialized = bv.toArray();
    System.out.println("Serialized length:" + serialized.length);
    BitVector bv2 = new BitVector(serialized);
    System.out.println("Bit count2: " + bv2.count());
    assertEquals(bv.count(), bv2.count());
  }

}