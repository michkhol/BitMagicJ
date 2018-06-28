package io.bitmagic;

import io.bitmagic.core.Strategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.function.BiFunction;

import static org.junit.Assert.*;


public class BitVectorTest {

  private static Random rand = new Random(10L);
  private static final int BSIZE = 150000000;
  private static final int REPEATS = 300;

  static boolean platform_test = true;

  // generate pseudo-random bit-vector, mix of compressed/non-compressed blocks
  static void generateBVector(BitVector bv, int vectorMax) {
    int i, j;
    for (i = 0; i < vectorMax;) {
      // generate bit-blocks
      for (j = 0; j < 65535 * 8; i += 10, j++) {
        bv.set(i, true);
      }
      if (i > vectorMax)
        break;
      // generate GAP (compressed) blocks
      for (j = 0; j < 65535; i += 120, j++) {
        int len = rand.nextInt(64);
        bv.setRange(i, i + len, true);
        i += len;
        if (i > vectorMax)
          break;
      }
    }
  }

  static void generateBVector(BitVector bv) {
    generateBVector(bv, 40000000);
  }

  //
// Interval filling.
// 111........111111........111111..........11111111.......1111111...
//
  static void fillSetsIntervals(BitVector bv, int min, int max, int fill_factor, boolean set_flag)
  {
    while(fill_factor==0)
    {
      fill_factor=rand.nextInt(10);
    }

    int i, j;
    int factor = 10 * fill_factor;
    for (i = min; i < max; ++i) {
      int len, end;

      do {
        len = rand.nextInt() % factor;
        end = i+len;

      } while (end >= max);

      for (j = i; j < end; ++j) {
        if (set_flag) {
          bv.set(j, true);
        }
        else {
          bv.set(j, false);
        }
      } // j

      i = end;
      len = rand.nextInt(10);

      i+=len;

      for(int k=0; k < 1000 && i < max; k+=3,i+=3) {
        if (set_flag)
        {
          bv.set(i, true);
        }
        else
        {
          bv.set(j, false);
        }
      }
    } // for i
  }

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
  public void BitCountTest()
  {
    BitVector  bv = new BitVector();
    int value = 0;

    fillSetsIntervals(bv, 0, BSIZE, 10, true);

    Timer tt = new Timer("BitCount. Random bitvector", REPEATS*20);
    for (int i = 0; i < REPEATS*20; ++i)
    {
      value += bv.count();
    }
    tt.report();
  }
}