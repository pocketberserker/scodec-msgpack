package scodec.msgpack
package codecs

import scalaz.{\/-, NonEmptyList}
import scalaz.syntax.either._
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs._

// TODO: fix foldLeft seed
private[codecs] class ChoiceCodec(codecs: NonEmptyList[Codec[MessagePack]])extends Codec[MessagePack] {

  override def encode(m: MessagePack) =
    NonEmptyList.nonEmptyList.foldLeft(codecs, "".left[BitVector])((acc, c) => acc.fold(_ => c.encode(m), \/-(_)))

  override def decode(buffer: BitVector) =
    NonEmptyList.nonEmptyList.foldLeft(codecs, "".left[(BitVector, MessagePack)])((acc, c) => acc.fold(_ => c.decode(buffer), \/-(_)))
}
