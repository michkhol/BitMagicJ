
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
    val bj = new BitJynx(bits)
    println(bj)
    bits.foreach { i =>
//      println(s"$i: ${bj.get(i)}")
      assert(bj.get(i), s"$i invalid")
    }
  }

  test("BitJynx empty") {
    val bits = new Array[Long](0)
    val bj = new BitJynx(bits)
    assert(bj.cardinality() == 0)
  }


  test("BitJynx AND") {
    val BitsNo = 1000000
    val RandLimit = 10000000
    val bits = new Array[Long](BitsNo)
    val rand = new Random()
    for(i <- 0 until BitsNo) {
      bits(i) = rand.nextInt(RandLimit)
      //      print(bits(i) + ",")
    }
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
    val bits = new Array[Long](BitsNo)
    val rand = new Random()
    for(i <- 0 until BitsNo) {
      bits(i) = rand.nextInt(RandLimit)
      //      print(bits(i) + ",")
    }
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
    assert(bj3.cardinality() == 2)

  }

}
