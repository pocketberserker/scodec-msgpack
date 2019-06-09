package scodec
package msgpack

import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import org.scalatestplus.scalacheck.Checkers
import scala.util.Random

class MessagePackSpec extends TestSuite with Checkers {

  import Serialize._

  "bool" should "be able to encode and decode" in {
    check(forAll((b: Boolean) => roundtrip(b)))
  }

  "int" should "be able to encode and decode" in {
    check(forAll((a: Int) => roundtrip(a)))
  }

  "long" should "be able to encode and decode" in {
    check(forAll((l: Long) => roundtrip(l)))
  }

  "float" should "be able to encode and decode" in {
    check(forAll((f: Float) => roundtrip(f)))
  }

  "double" should "be able to encode and decode" in {
    check(forAll((d: Double) => roundtrip(d)))
  }

  "string" should "be able to encode and decode" in {
    check(forAll((s: String) => roundtrip(s)))
  }

  "array" should "be able to encode and decode" in {
    check(forAll((a: Vector[Int]) => roundtrip(a)))
  }

  "map" should "be able to encode and decode" in {
    check(forAll((m: Map[Int, Int]) => roundtrip(m)))
  }

  case class Point(x: Int, y: Int, z: Int)

  "Point" should "be able to encode and decode" in {
    roundtrip(Point(1000, 5, 2))
  }

  "binary 16" should "be able to decode" in {
    val data = Array.fill[Byte](266)(Random.nextInt.toByte)
    val binary = Array(0xc5, 0x01, 0x0a).map(_.toByte) ++ data
    val result = codecs.MessagePackCodec.decode(scodec.bits.BitVector(binary))
    assert(result.map(_.value) == Attempt.successful(MBinary16(scodec.bits.ByteVector(data))))
  }

}
