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


}