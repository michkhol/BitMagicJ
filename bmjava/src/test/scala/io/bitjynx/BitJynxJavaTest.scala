package io.bitjynx

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util
import java.util.stream.{IntStream, LongStream, StreamSupport}

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Random
import scala.collection.JavaConverters._
import scala.collection.SortedMap
import scala.util.control.NonFatal

object BitJynxJavaTest {
  def bitPosBlock(v: Short*): BitPosBlock = {
    new BitPosBlock(v.toArray)
  }

  def makeEvenBlock: BitPosBlock = {
    val len = BitJynx.BITS_PER_BLOCK >> 1
    val posArr = new Array[Short](len)
    for(i <- 0 until len) {
      posArr(i) = (i << 1).asInstanceOf[Short]
    }
    new BitPosBlock(posArr)
  }

  def makeOddBlock: BitPosBlock = {
    val len = BitJynx.BITS_PER_BLOCK >> 1
    val posArr = new Array[Short](len)
    for(i <- 0 until len) {
      posArr(i) = ((i << 1) + 1).asInstanceOf[Short]
    }
    new BitPosBlock(posArr)
  }

  def createRandom(nOfBits: Int, randLimit: Int): Array[Int] = {
    val bits = new Array[Int](nOfBits)
    val rand = new Random()
    for(i <- 0 until nOfBits) {
      bits(i) = rand.nextInt(randLimit)
    }
    util.Arrays.parallelSort(bits)
    IntStream.of(bits: _*).distinct().toArray
  }

  def bitMapBlock(v: Int*): BitMapBlock = {
    new BitMapBlock(v.toArray)
  }
}

class BitJynxJavaTest extends FunSuite with BeforeAndAfterAll {
  import BitJynxJavaTest._

  override def beforeAll(): Unit = {
//    import java.io.BufferedReader
//    import java.io.InputStreamReader
//    val br = new BufferedReader(new InputStreamReader(System.in))
//    System.out.print("Enter:")
//    val s = br.readLine
  }

  test("BitMapBlock - serialization") {
    val a = Array[Int](0, 1, 24, 35, 540, 3456, 8000)
    val b = bitMapBlock(a:_*)
    val ser = b.stream().toArray
    assert(a.sameElements(ser))
  }

  test("BitPosBlock - and") {

    // With BitPosBlock
    val b1 = bitPosBlock(0, 1, 24, 35, 540, 3456, 8000)
    assert(b1.and(EmptyBlock.instance).eq(EmptyBlock.instance))

    val withUnity = b1.and(UnityBlock.instance)
    assert(b1.ne(withUnity) && b1 == withUnity)

    val b2 = bitPosBlock(5, 24, 35, 128, 350, 2756, 8003)
    assert(b1.and(b2).cardinality() == 2)

    val b3 = bitPosBlock(2, 500, 7890)
    assert(b1.and(b3).eq(EmptyBlock.instance))

    val even = makeEvenBlock
    val odd = makeOddBlock
    assert(even.and(odd).eq(EmptyBlock.instance))

    // With ZeroPosBlock
    val zb2 = b2.not()
    assert(b1.and(zb2).cardinality() == 5)

    val x = even.and(odd.not())
    assert(x.isInstanceOf[BitPosBlock])
    assert(AbstractBitPosBlock.isEquivalent(x.asInstanceOf[BitPosBlock], even))
  }

  test("BitPosBlock - or") {
    // With BitPosBlock
    val b1 = bitPosBlock(0, 1, 24, 35, 540, 3456, 8000)
    assert(b1.or(UnityBlock.instance).eq(UnityBlock.instance))

    val withEmpty = b1.or(EmptyBlock.instance)
    assert(b1.ne(withEmpty) && b1 == withEmpty)

    val b2 = bitPosBlock(5, 24, 35, 128, 350, 2756, 8003)
    assert(b1.or(b2).cardinality() == 12)

    val b3 = bitPosBlock(2, 500, 7890)
    assert(b1.or(b3).cardinality() ==  10)

    val even = makeEvenBlock
    val odd = makeOddBlock
    assert(even.or(odd).eq(UnityBlock.instance))

    // With ZeroPosBlock
    val zb2 = b2.not()
    assert(b1.or(zb2).cardinality() == BitJynx.BITS_PER_BLOCK - 5)

    val x = even.or(odd.not())
    assert(x.isInstanceOf[ZeroPosBlock])
    assert(AbstractBitPosBlock.isEquivalent(x.asInstanceOf[ZeroPosBlock], even))
  }

  test("BitPosBlock - xor") {
    val b1 = bitPosBlock(0, 1, 24, 35, 540, 3456, 8000)
    val withUnity = b1.xor(UnityBlock.instance)
    assert(withUnity.isInstanceOf[ZeroPosBlock])
    assert(withUnity.cardinality() == BitJynx.BITS_PER_BLOCK - b1.cardinality())

    val withEmpty = b1.or(EmptyBlock.instance)
    assert(b1.ne(withEmpty) && b1 == withEmpty)

    val b2 = bitPosBlock(5, 24, 35, 128, 350, 2756, 8003)
    assert(b1.xor(b2).cardinality() == 10)

    val b3 = bitPosBlock(2, 500, 7890)
    assert(b1.xor(b3).cardinality() ==  10)

    val even = makeEvenBlock
    val odd = makeOddBlock
    assert(even.xor(odd).eq(UnityBlock.instance))
    assert(even.xor(even).eq(EmptyBlock.instance))

    // With ZeroPosBlock
    val zb2 = b2.not()
    assert(b1.xor(zb2).cardinality() == BitJynx.BITS_PER_BLOCK - 10)

    val x = even.xor(odd.not())
    assert(x.eq(EmptyBlock.instance))
  }

  test("BitPosBlock - nand") {
    val b1 = bitPosBlock(0, 1, 24, 35, 540, 3456, 8000)
    assert(b1.nand(EmptyBlock.instance).eq(UnityBlock.instance))

    val withUnity = b1.nand(UnityBlock.instance)
    assert(withUnity.isInstanceOf[ZeroPosBlock])
    assert(b1 == withUnity.not())

    val b2 = bitPosBlock(5, 24, 35, 128, 350, 2756, 8003)
    val nand1 = b1.nand(b2)
    assert(nand1.isInstanceOf[ZeroPosBlock])
    assert(nand1.cardinality() == BitJynx.BITS_PER_BLOCK - 2)

    val even = makeEvenBlock
    val odd = makeOddBlock
    assert(even.nand(odd).eq(UnityBlock.instance))

    // With ZeroPosBlock
    val zb2 = b2.not()
    assert(b1.nand(zb2).cardinality() == BitJynx.BITS_PER_BLOCK - 5)

    val x = even.nand(odd.not())
    assert(x.isInstanceOf[ZeroPosBlock])
    assert(AbstractBitPosBlock.isEquivalent(x.asInstanceOf[ZeroPosBlock], odd))
  }

  test("BitJynx create") {
    val start = System.currentTimeMillis()
    val BitsNo = 10000000
    val RandLimit = 100000000
    val bits = createRandom(BitsNo, RandLimit)
    val bj = new BitJynx(bits)
    println(bj)
    val created = System.currentTimeMillis()
    println(s"Created took: ${created - start} ms.")
    bits.foreach { i =>
//      println(s"$i: ${bj.getAsIntArray(i)}")
      assert(bj.get(i), s"at $i")
    }
    val end = System.currentTimeMillis()
    println(s"Bit check took: ${end - created} ms.")
  }

  test("BitJynx serialize") {
    val start = System.currentTimeMillis()
    val BitsNo = 1000000
    val RandLimit = 100000000
    val bits = createRandom(BitsNo, RandLimit)
    val bj = new BitJynx(bits)
    println(bj)
    val created = System.currentTimeMillis()
    println(s"Created took: ${created - start} ms.")
    val ser = bj.toArray
    println(s"Serialized size: ${ser.length}")
//    for(i <- 0 until bits.length) {
//      if (bits(i) != ser(i)) {
//        println(s"Mismatch at $i, original: ${bits(i)}, serialized: ${ser(i)}")
//        throw new RuntimeException("Stop")
//      }
//    }
    assert(bits.sameElements(bj.toArray))
    val end = System.currentTimeMillis()
    println(s"Array check took: ${end - created} ms.")
  }

  test("BitJynx serialize with limit") {
    val start = System.currentTimeMillis()
    val BitsNo = 10000000
    val RandLimit = 100000000
    val bits = createRandom(BitsNo, RandLimit)
    val bj = new BitJynx(bits)
    println(bj)
    val created = System.currentTimeMillis()
    println(s"Created took: ${created - start} ms.")
    val ser = bj.toArray
    println(s"Serialized size: ${ser.length}")
    assert(bits.sameElements(bj.toArray))
    val end = System.currentTimeMillis()
    println(s"Array check took: ${end - created} ms.")
  }

  test("BitJynx empty") {
    val bits = new Array[Int](0)
    val bj = new BitJynx(bits)
    assert(bj.cardinality() == 0)
  }


  test("BitJynx AND") {
    val BitsNo = 1000000
    val RandLimit = 10000000
    val bits = createRandom(BitsNo, RandLimit)

    val bj = new BitJynx(bits)
    println(bj)

    val bits1 = Array[Int](1,2,3,5,6, 60000, 80000)
    val bj1 = new BitJynx(bits1)
    println(bj1)

    val bits2 = Array[Int](2, 1001,3001,60000)
    val bj2 = new BitJynx(bits2)
    println(bj2)

    val bj3 = bj1.and(bj2)
    println(bj3)
    assert(bj3.cardinality() == 2)

    val bj4 = bj2.and(bj)
    println(bj4)
    val bj5 = bj.and(bj2)
    println(bj5)
  }

  test("BitJynx OR") {
    val BitsNo = 1000000
    val RandLimit = 10000000
    val bits = createRandom(BitsNo, RandLimit)
    val bj = new BitJynx(bits)
    println(bj)

    val bits1 = Array[Int](1,2,3,5,6, 60000, 80000)
    val bj1 = new BitJynx(bits1)
    println(bj1)

    val bits2 = Array[Int](2, 1001,3001,60000)
    val bj2 = new BitJynx(bits2)
    println(bj2)

    val bj3 = bj1.or(bj2)
    println(bj3)
    assert(bj3.cardinality() == 9)

  }

  test("BitJynx SNP") {
    val r = """^.+\s+(\d+)\s+rs(\d+)""".r
    val p = Paths.get("chr1.vcf")
    var counter = 0
    try {
      var rsMap = Files.lines(p, StandardCharsets.ISO_8859_1).iterator().asScala.take(5000000).flatMap { s =>
        counter = counter + 1
        if (counter % 1000000 == 0) println(s"Lines read: $counter")
        for (m <- r.findFirstMatchIn(s)) yield (m.group(1).toInt, m.group(2).toInt)
      }.toMap
      var sortedRsMap = SortedMap[Int, Int]() ++ rsMap
      rsMap = null // gc away
      println("RS map size: " + sortedRsMap.size)
      val posArray = sortedRsMap.keys.toArray

      val ts = System.currentTimeMillis()
      val b0 = new BitJynx(posArray)
      val bm = new Array[BitJynx](32)
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
        bm(i) = if (idx == 0) BitJynx.empty else new BitJynx(buf, idx)
      }
      val ts2 = System.currentTimeMillis()
      println(s"Create time: ${ts2 - ts} ms")
      println("Positions vector: " + b0.toString)
//      printBm(bm)
      sortedRsMap = null // gc away

      // And with an rs
      val rs = 1003651171
      // Warmup
//      println("And warmup")
//      for(i <- 0 until 5) {
//        val bmAnd = new Array[BitJynx](32)
//        for (i <- 0 until 32) {
//          val mask = 1 << i
//          if ((rs & mask) != 0)
//            bmAnd(i) = b0.and(bm(i))
//          else
//            bmAnd(i) = BitJynx.empty
//        }
//      }
      println("Cleaning up")
      System.gc()
      // Measure
      println("And measure start")
      val tsw = System.currentTimeMillis()
      val reps = 10
      val bmAnd = Array.ofDim[BitJynx](reps, 32)
      for(i <- 0 until reps) {
        for (j <- 0 until 32) {
          val mask = 1 << j
          if ((rs & mask) != 0)
            bmAnd(i)(j) = b0.and(bm(j))
          else
            bmAnd(i)(j) = BitJynx.empty
        }
      }

      val ts3 = System.currentTimeMillis()
      println(s"And time: ${(ts3 - tsw) / reps} ms")
//      printBm(bmAnd(0))

//      val bmXor = new Array[BitJynx](32)
//      for(i <- 0 until 32) {
//        val mask = 1 << i
//        if ((rs & mask) != 0)
//          bmXor(i) = b0.xor(bm(i))
//        else
//          bmXor(i) = bm(i)
//      }
//      val ts4 = System.currentTimeMillis()
//      println
//      println(s"Xor time: ${ts4 - ts3} ms")
//      printBm(bmXor)
    }
    catch {
      case NonFatal(e) =>
        println(s"Error at $counter")
        e.printStackTrace()
    }
  }

  private def printBm(bm: Array[BitJynx]): Unit = {
    for (i <- bm.indices)
      println(s"Bit $i: " + bm(i).toString)
  }

}
