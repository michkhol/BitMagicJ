
import io.bitjynx.BitJynx
import io.bitmagic.core.{OptMode, Strategy}
import io.bitmagic.java.BitVector
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.collection.JavaConverters._
import scala.util.Random

class BitJynxJavaTest extends FunSuite with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
//    import java.io.BufferedReader
//    import java.io.InputStreamReader
//    val br = new BufferedReader(new InputStreamReader(System.in))
//    System.out.print("Enter:")
//    val s = br.readLine
  }



  test("BitJynx create") {
    val BitsNo = 1000000
    val RandLimit = 10000000
    val bits = new Array[Long](BitsNo)
    val rand = new Random()
    for(i <- 0 until BitsNo) {
      bits(i) = rand.nextInt(RandLimit)
//      print(bits(i) + ",")
    }
    println()
    val bj = new BitJynx(bits)
    println(bj)
    bits.foreach { i =>
//      println(s"$i: ${bj.get(i)}")
      assert(bj.get(i), s"$i invalid")
    }
  }

//  test("Set and retrieval") {
//    val bv = new BitVector(1,3,5)
//    assert(bv.get(1))
//    assert(!bv.get(2))
//    bv.set(2, true)
//    assert(bv.get(2))
//  }
//
//  test("Optimize") {
//    val bv = new BitVector(Strategy.BM_BIT, 1,3,5, 560, 2346, 5876, 8458, 1234567, 2345678)
//
//    println("Bit count: " + bv.count())
//    var stat = bv.calcStat()
//    println(stat)
//    stat = bv.optimize(OptMode.FREE_EMPTY_BLOCKS)
//    println(stat)
//  }
//
//  test("Iterator") {
//    val bv = new BitVector(Strategy.BM_BIT, 1,3,5, 560, 2346, 5876, 8458, 1234567, 2345678)
//
//    for(i <- bv.iterator().asScala) {
//      print(i)
//      print(" ")
//    }
//    println()
//  }
//
//  test("Serialization") {
//    var bv = new BitVector()
//    var bit = true
//    val limit: Long = 100000000
////    val limit: Long = Integer.MAX_VALUE.asInstanceOf[Long] << 1
//    var idx = 0L
//    while(idx < limit) {
//      bit = !bit
//      bv.set(idx, bit)
//      idx += 1
//    }
//    println("Bit count1: " + bv.count)
//    val serialized = bv.toArray
//    println("Serialized length:" + serialized.length)
//    val bv2 = new BitVector(serialized)
//    println("Bit count2: " + bv2.count)
//    assert(bv.count == bv2.count)
//  }

}
