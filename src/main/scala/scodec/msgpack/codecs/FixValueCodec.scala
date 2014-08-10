package scodec.msgpack
package codecs

import scalaz.-\/
import scalaz.syntax.std.option._
import scodec.Codec
import scodec.bits.BitVector

private[codecs] class FixValueCodec[A](b: Codec[BitVector], head: BitVector, body: Codec[A],
  apply: A => MessagePack, unapply: MessagePack => Option[A], name: String)
  extends Codec[MessagePack] {

  override def encode(m: MessagePack) = for {
    h <- b.encode(head)
    v <- unapply(m).toRightDisjunction("fail to unapply ${$name}.")
    encodeM <- body.encode(v)
  } yield h ++ encodeM

  override def decode(buffer: BitVector) =
    b.decode(buffer).flatMap { case (rest, h) =>
      if(h.startsWith(head)) body.decode(rest).map { case (r, a) => (r, apply(a)) }
     else -\/("first bytes did not match ${name}.")
    }
}
