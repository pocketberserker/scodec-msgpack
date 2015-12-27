package scodec
package msgpack

import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.math.BigInteger

import org.msgpack.core.MessagePack.Code
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalatest.FlatSpec
import org.scalatest.prop.Checkers
import scodec.bits.BitVector

class NumberSpec extends FlatSpec with Checkers {

  private[this] def withDataOutputStream(f: DataOutputStream => Unit): BitVector = {
    val out = new ByteArrayOutputStream()
    val data = new DataOutputStream(out)
    f(data)
    data.close()
    BitVector(out.toByteArray)
  }

  private[this] def toUnsignedBigInt(i: Long): BigInt = {
    val upper = (i >>> 32).toInt
    val lower = i.toInt

    val u = BigInteger.valueOf(upper.toLong & 0xffffffffL).shiftLeft(32)
    val l = BigInteger.valueOf(lower.toLong & 0xffffffffL)
    u add l
  }

  private[this] val params = Seq(MinSuccessful(1000))

  "INT64" should "be able to decode" in {
    check({ x: Long =>
      val bytes = withDataOutputStream { out =>
        out.writeByte(Code.INT64)
        out.writeLong(x)
      }
      if (Int.MinValue <= x && x <= Int.MaxValue) {
        assert(Codec[Int].decodeValue(bytes) == Attempt.Successful(x))
      } else {
        assert(Codec[Int].decodeValue(bytes).isFailure)
      }
      assert(Codec[Long].decodeValue(bytes) == Attempt.Successful(x))
      true
    }, params: _*)
  }

  "UINT32" should "be able to decode" in {
    val gen = Gen.frequency(
      100 -> {
        for {
          x <- Gen.choose(0, Int.MaxValue)
          y <- Gen.choose(0, Int.MaxValue)
        } yield x.toLong + y.toLong
      },
      1 -> Gen.const(0L),
      1 -> Gen.const(1L),
      1 -> Gen.const(Int.MaxValue.toLong - 1),
      1 -> Gen.const(Int.MaxValue.toLong),
      1 -> Gen.const(Int.MaxValue.toLong + 1),
      1 -> Gen.const(Int.MaxValue.toLong * 2 - 1),
      1 -> Gen.const(Int.MaxValue.toLong * 2),
      1 -> Gen.const(Int.MaxValue.toLong * 2 + 1)
    )

    check(forAll(gen) { x =>
      val bytes = withDataOutputStream { out =>
        out.writeByte(Code.UINT32)
        out.writeInt(x.toInt)
      }
      if (Int.MinValue <= x && x <= Int.MaxValue) {
        assert(Codec[Int].decodeValue(bytes) == Attempt.Successful(x))
      } else {
        assert(Codec[Int].decodeValue(bytes).isFailure)
      }
      assert(Codec[Long].decodeValue(bytes) == Attempt.Successful(x))
      true
    }, params: _*)
  }

  "UINT64" should "be able to decode" in {
    val gen = Gen.frequency(
      100 -> {
        for {
          x <- Gen.choose(0, Long.MaxValue)
          y <- Gen.choose(0, Long.MaxValue)
        } yield BigInt(x) + y
      },
      1 -> Gen.const(BigInt(0)),
      1 -> Gen.const(BigInt(1)),
      1 -> Gen.const(BigInt(Long.MaxValue) - 1),
      1 -> Gen.const(BigInt(Long.MaxValue)),
      1 -> Gen.const(BigInt(Long.MaxValue) + 1),
      1 -> Gen.const(BigInt(Long.MaxValue) * 2 - 1),
      1 -> Gen.const(BigInt(Long.MaxValue) * 2),
      1 -> Gen.const(BigInt(Long.MaxValue) * 2 + 1)
    )

    check(forAll(gen) { x =>
      val bytes = withDataOutputStream { out =>
        out.writeByte(Code.UINT64)
        out.writeLong(x.toLong)
      }
      if (Int.MinValue <= x && x <= Int.MaxValue) {
        assert(Codec[Int].decodeValue(bytes) == Attempt.Successful(x))
      } else {
        assert(Codec[Int].decodeValue(bytes).isFailure)
      }
      if (Long.MinValue <= x && x <= Long.MaxValue) {
        assert(Codec[Long].decodeValue(bytes) == Attempt.Successful(x))
      } else {
        assert(Codec[Long].decodeValue(bytes).map(toUnsignedBigInt) == Attempt.Successful(x))
      }
      true
    }, params: _*)
  }

  "FLOAT32" should "be able to decode" in {
    check({ x: Float =>
      val bytes = withDataOutputStream { out =>
        out.writeByte(Code.FLOAT32)
        out.writeFloat(x)
      }
      assert(Codec[Float].decodeValue(bytes) == Attempt.Successful(x))
      assert(Codec[Double].decodeValue(bytes) == Attempt.Successful(x))
      true
    }, params: _*)
  }

}
