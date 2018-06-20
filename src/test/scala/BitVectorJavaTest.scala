
import io.bitmagic.core.Strategy
import io.bitmagic.core.{OptMode, Strategy}
import io.bitmagic.BitVector
import org.scalactic.source.Position
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}

import scala.collection.JavaConverters._
import scala.util.Random

object BitVectorJavaTest {

  // Generators

  // generate pseudo-random bit-vector, mix of compressed/non-compressed blocks
  def generateBVector(vectorMax: Int = 40000000): BitVector = {
    val bv = new BitVector()
    var i, j: Int = 0
    do {
      // generate bit blocks
      do {
        bv.set(i, true)
        i += 10
        j += 1
      } while(j < 65535 * 8 && i < vectorMax)
      if (i >= vectorMax)
        return bv
      // Generate GAP (compressed) blocks
      j = 0
      do {
        val len = Random.nextInt(64)
        bv.setRange(i, i + len, true)
        i += len
        if (i > vectorMax)
          return bv

        j += 1
        i += 120
      } while(j < 65535)
    } while(i < vectorMax)
  }

  // Interval filling.
  // 111........111111........111111..........11111111.......1111111...
  def intervalFilling(min: Int, max: Int, fillFactor: Int, setFlag: Boolean = true): BitVector = {
    val bv = new BitVector()
    var ff = fillFactor
    while(ff == 0) ff = Random.nextInt(10)
    var i = min
    var j: Int = 0
    val factor = 10 * ff
    while (i < max) {
      var len, end: Int = 0
      do {
        len = Random.nextInt(factor)
        end += i + len
      } while (end >= max)
      j = i
      while (j < end) {
        bv.set(j, setFlag)
        j += 1
      }
      i = end
      len = Random.nextInt(10)
      i += len
      var k = 0
      while(k < 1000 && i < max) {
        bv.set(i, setFlag)
        k +=3
        i +=3
      }
      i +=1
    }
    bv
  }
}

class BitVectorJavaTest extends FunSuite with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
//    import java.io.BufferedReader
//    import java.io.InputStreamReader
//    val br = new BufferedReader(new InputStreamReader(System.in))
//    System.out.print("Enter:")
//    val s = br.readLine
  }


  test("BitMagic version") {
    val version = BitVector.getVersion
    println("Bitmagic version " + version)
  }

  test("BitVector size") {
    val bSize = 10000
    val bSize2 = 13445
    val bv = new BitVector(bSize, Strategy.BM_BIT)
    val sz = bv.getSize
    println(s"BitVector size: $sz")
    assert(sz == bSize)
    bv.setSize(bSize2)
    assert(bv.getSize == bSize2)
  }

  test("Set and retrieve") {
    val bv = new BitVector(1,3,5)
    assert(bv.get(1))
    assert(!bv.get(2))
    bv.set(2, true)
    assert(bv.get(2))
  }

  test("Increment") {
    val bv = new BitVector(1, 3, 500000000)
    assert(bv.inc(2) == 0)
    assert(bv.inc(500000000) == 1)
  }

  test("Find first") {
    val bv = new BitVector(39000, 67000000, 500000000)
    assert(bv.findFirst() == 39000)
  }

  test("Find reverse") {
    val bv = new BitVector(39000, 67000000, 500000000)
    bv.set(500000000, false)
    assert(bv.findReverse() == 67000000)
  }

  test("Optimize") {
    val bv = new BitVector(Strategy.BM_BIT, 1,3,5, 560, 2346, 5876, 8458, 1234567, 2345678)

    println("Bit count: " + bv.count())
    var stat = bv.calcStat()
    println(stat)
    stat = bv.optimize(OptMode.FREE_EMPTY_BLOCKS)
    println(stat)
  }

  test("Iterator") {
    val bv = new BitVector(Strategy.BM_BIT, 1,3,5, 560, 2346, 5876, 8458, 1234567, 2345678)

    for(i <- bv.iterator().asScala) {
      print(i)
      print(" ")
    }
    println()
  }

  test("Serialization") {
    var bv = new BitVector()
    var bit = true
    val limit: Long = 100000000
//    val limit: Long = Integer.MAX_VALUE.asInstanceOf[Long] << 1
    var idx = 0L
    while(idx < limit) {
      bit = !bit
      bv.set(idx, bit)
      idx += 1
    }
    println("Bit count1: " + bv.count)
    val serialized = bv.toArray
    println("Serialized length:" + serialized.length)
    val bv2 = new BitVector(serialized)
    println("Bit count2: " + bv2.count)
    assert(bv.count == bv2.count)
  }

}
