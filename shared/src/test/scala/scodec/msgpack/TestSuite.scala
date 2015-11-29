package scodec.msgpack

import scodec._
import Attempt._
import scodec.bits.BitVector
import org.scalatest.FlatSpec
import org.scalatest.DiagrammedAssertions

abstract class TestSuite extends FlatSpec with DiagrammedAssertions {

  def roundtrip[A](a: A)(implicit C: Codec[A]) = {
    C.encode(a) match {
      case Failure(error) =>
        fail(error.toString())
      case Successful(encoded) =>
        C.decode(encoded) match {
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

