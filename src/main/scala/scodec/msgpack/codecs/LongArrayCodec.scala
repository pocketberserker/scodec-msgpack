package scodec.msgpack
package codecs

import scala.collection.immutable.IndexedSeq
import scalaz.{\/, \/-, -\/}
import scalaz.std.indexedSeq._
import scalaz.syntax.traverse._
import scalaz.syntax.std.option._
import scodec.Codec
import scodec.bits.BitVector

// FIXME: type conversion
private[codecs] class LongArrayCodec(size: Codec[Long]) extends Codec[IndexedSeq[MessagePack]] {

  override def encode(s: IndexedSeq[MessagePack]) = for {
    a <- s.traverseU { v => MessagePackCodec.encode(v) }.map { _.concatenate }
    n <- size.encode(s.length.toLong)
  } yield n ++ a

  override def decode(buffer: BitVector) =
    size.decode(buffer).flatMap { case (r, n) => {
      val builder = Vector.newBuilder[MessagePack]
      var remaining = r
      var error: Option[String] = None
      for(_ <- 0 to n.toInt - 1) {
        MessagePackCodec.decode(remaining) match {
          case \/-((rest, value)) =>
            builder += value
            remaining = rest
          case -\/(err) =>
            error = Some(err)
            remaining = BitVector.empty
        }
      }
      error.toLeftDisjunction((BitVector.empty, builder.result))
    }}
}
