package scodec.msgpack

import scodec._
import Attempt._
import scodec.bits.BitVector
import org.scalatest.FlatSpec
import org.scalatest.DiagrammedAssertions

abstract class TestSuite extends FlatSpec with DiagrammedAssertions {

  def roundtrip[A](codec: Codec[A], a: A) = {
    codec.encode(a) match {
      case Failure(error) =>
        fail(error.toString())
      case Successful(encoded) =>
        codec.decode(encoded) match {
          case Failure(error) =>
            fail(error.toString())
          case Successful(DecodeResult(decoded, remainder)) =>
            assert(remainder === BitVector.empty)
            assert(decoded === a)
            decoded === a
        }
    }
  }

}

