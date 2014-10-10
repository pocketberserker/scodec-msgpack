package scodec.msgpack

import scodec._
import scodec.bits.BitVector
import org.scalatest.FlatSpec
import org.scalatest.DiagrammedAssertions
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import org.scalatest.prop.Checkers
import MessagePackArbitrary._

class MessagePackSpec extends FlatSpec with DiagrammedAssertions with Checkers {

  // https://github.com/scodec/scodec/blob/ae93a6e30a2a85796586c64ec7b84b1527c488ef/src/test/scala/scodec/CodecSuite.scala#L18
  def roundtrip[A](codec: Codec[A], a: A) = {
    val encoded = codec.encode(a)
    assert(encoded.isRight)
    val (remainder, decoded) = codec.decode(encoded.toOption.get).toEither.right.get
    assert(remainder === BitVector.empty)
    decoded === a
  }

  "bool" should "be able to encode and decode" in {
    check(forAll((b: Boolean) => roundtrip(msgpack.bool, b)))
  }

  "int" should "be able to encode and decode" in {
    check(forAll((a: Int) => roundtrip(msgpack.int, a)))
  }

  "long" should "be able to encode and decode" in {
    check(forAll((l: Long) => roundtrip(msgpack.long, l)))
  }

  "string" should "be able to encode and decode" in {
    check(forAll(genASCII)((s: String) => roundtrip(msgpack.str, s)))
  }

  implicit val intSerializer = Serialize.int

  "array" should "be able to encode and decode" in {
    check(forAll((a: Vector[Int]) => roundtrip(msgpack.array, a)))
  }

  "map" should "be able to encode and decode" in {
    check(forAll((m: Map[Int, Int]) => roundtrip(msgpack.map, m)))
  }

  case class Point(x: Int, y: Int, z: Int)
  val pointCodec = (msgpack.int :: msgpack.int :: msgpack.int).as[Point]

  "Point" should "be able to encode and decode" in {
    roundtrip(pointCodec, Point(1000, 5, 2))
  }
}
