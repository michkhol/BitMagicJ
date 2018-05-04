package io.bitjynx

import java.util
import java.util.stream.LongStream

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Random

class BitJynxJavaTest extends FunSuite with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
//    import java.io.BufferedReader
//    import java.io.InputStreamReader
//    val br = new BufferedReader(new InputStreamReader(System.in))
//    System.out.print("Enter:")
//    val s = br.readLine
  }

  def createRandom(nOfBits: Int, randLimit: Int): Array[Long] = {
    val bits = new Array[Long](nOfBits)
    val rand = new Random()
    for(i <- 0 until nOfBits) {
      bits(i) = rand.nextInt(randLimit)
    }
    util.Arrays.parallelSort(bits)
    LongStream.of(bits: _*).distinct().toArray
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
//      println(s"$i: ${bj.getAsLongArray(i)}")
      assert(bj.get(i), s"at $i")
    }
    val end = System.currentTimeMillis()
    println(s"Bit check took: ${end - created} ms.")
  }

  test("BitJynx serialize") {
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

  test("BitJynx empty") {
    val bits = new Array[Long](0)
    val bj = new BitJynx(bits)
    assert(bj.cardinality() == 0)
  }


  test("BitJynx AND") {
    val BitsNo = 1000000
    val RandLimit = 10000000
    val bits = createRandom(BitsNo, RandLimit)

    val bj = new BitJynx(bits)
    println(bj)

    val bits1 = Array[Long](1,2,3,5,6, 60000, 80000)
    val bj1 = new BitJynx(bits1)
    println(bj1)

    val bits2 = Array[Long](2, 1001,3001,60000)
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

    val bits1 = Array[Long](1,2,3,5,6, 60000, 80000)
    val bj1 = new BitJynx(bits1)
    println(bj1)

    val bits2 = Array[Long](2, 1001,3001,60000)
    val bj2 = new BitJynx(bits2)
    println(bj2)

    val bj3 = bj1.or(bj2)
    println(bj3)
    assert(bj3.cardinality() == 9)

  }

}
