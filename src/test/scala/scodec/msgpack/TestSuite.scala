package scodec.msgpack

import scodec._
import scodec.bits.BitVector
import org.scalatest.FlatSpec
import org.scalatest.DiagrammedAssertions

class TestSuite extends FlatSpec with DiagrammedAssertions {

  // https://github.com/scodec/scodec/blob/ae93a6e30a2a85796586c64ec7b84b1527c488ef/src/test/scala/scodec/CodecSuite.scala#L18
  def roundtrip[A](codec: Codec[A], a: A) = {
    val encoded = codec.encode(a)
    assert(encoded.isRight)
    val (remainder, decoded) = codec.decode(encoded.toOption.get).toEither.right.get
    assert(remainder === BitVector.empty)
    decoded === a
  }
}

