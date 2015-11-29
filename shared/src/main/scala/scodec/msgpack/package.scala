package scodec

import scodec.bits.BitVector

package object msgpack {

  import codecs.MessagePackCodec

  implicit def serializeCodec[A](implicit S: Serialize[A]): Codec[A] = new Codec[A] {
    def encode(a: A): Attempt[BitVector] = S.pack(a).flatMap(MessagePackCodec.encode)
    def decode(buffer: BitVector): Attempt[DecodeResult[A]] =
      MessagePackCodec.decode(buffer).flatMap {
        case DecodeResult(a, rest) => S.unpack(a).map(DecodeResult(_, rest))
      }
    def sizeBound = MessagePackCodec.sizeBound
  }
}
