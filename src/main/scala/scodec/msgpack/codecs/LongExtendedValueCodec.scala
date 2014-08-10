package scodec.msgpack
package codecs

import scalaz.\/
import scodec.Codec
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._

// FIXME: type conversion
private[codecs] class LongExtendedValueCodec(size: Codec[Long]) extends Codec[(ByteVector, ByteVector)] {

  def fail[A](a: A, msg: String): String =
    s"[$a] is too long to be encoded: $msg"

  override def encode(v: (ByteVector, ByteVector)) = for {
    encodeData <- bytes.encode(v._2)
    encodeCode <- bytes(1).encode(v._1)
    encodeSize <- encodeData.intSize.map { n => size.encode(n.toLong).leftMap { fail(v, _) } }
      .getOrElse(\/.left(fail(v, s"${encodeData.size} exceeds maximum 32-bit integer value ${Int.MaxValue}")))
  } yield (encodeSize ++ encodeCode ++ encodeData)

  override def decode(buffer: BitVector) =
    size.decode(buffer).flatMap { case (rest, n) => (bytes(1) ~ bytes(n.toInt)).decode(rest) }
}
