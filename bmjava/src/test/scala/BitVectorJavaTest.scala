
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import io.bitmagic.core.Strategy
import io.bitmagic.core.{OptMode, Strategy}
import io.bitmagic.java.BitVector
import org.scalactic.source.Position
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}

import scala.collection.JavaConverters._
import scala.collection.SortedMap
import scala.util.control.NonFatal

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

  test("BitVector SNP") {
    val r = """^.+\s+(\d+)\s+rs(\d+)""".r
    val p = Paths.get("chr1.vcf")
    var counter = 0
    try {
      var rsMap = Files.lines(p, StandardCharsets.ISO_8859_1).iterator().asScala.take(5000000).flatMap { s =>
        counter = counter + 1
        if (counter % 1000000 == 0) println(s"Lines read: $counter")
        for (m <- r.findFirstMatchIn(s)) yield (m.group(1).toInt, m.group(2).toInt)
      }.toMap
      var sortedRsMap: SortedMap[Int, Int] = SortedMap[Int, Int]() ++ rsMap
      rsMap = null // gc away
      println("RS map size: " + sortedRsMap.size)
      val posArray = sortedRsMap.keys.toArray

      val ts = System.currentTimeMillis()
      //      val ibm = new IntVector(sortedRsMap.toMap.asJava)
      val b0 = new BitVector(posArray:_*)
      val bm = new Array[BitVector](32)
      val buf = new Array[Int](sortedRsMap.size)
      for(i <- 0 until 32) {
        val mask = 1 << i
        var idx = 0
        sortedRsMap.foreach { case(k, v) =>
          if ((v & mask) != 0) {
            buf(idx) = k
            idx = idx + 1
          }
        }
        bm(i) = if (idx == 0) BitVector.empty else new BitVector(buf.take(idx):_*)
      }
      val ts2 = System.currentTimeMillis()
      println(s"Create time: ${ts2 - ts} ms")
      sortedRsMap = null // gc away

      // And with an rs
      val rs = 1003651171
      println("Cleaning up")
      System.gc()
      // Measure
      println("And measure start")
      val tsw = System.currentTimeMillis()
      val reps = 10
      val bmAnd = Array.ofDim[BitVector](reps, 32)
      for(i <- 0 until reps) {
        for (j <- 0 until 32) {
          val mask = 1 << j
          if ((rs & mask) != 0) {
            val cp = b0.copy
            cp.and(bm(j))
            bmAnd(i)(j) = cp
          }
          else
            bmAnd(i)(j) = BitVector.empty
        }
      }

      val ts3 = System.currentTimeMillis()
      println(s"And time: ${(ts3 - tsw) / reps} ms")

    }
    catch {
      case NonFatal(e) =>
        println(s"Error at $counter")
        e.printStackTrace()
    }
  }
}
