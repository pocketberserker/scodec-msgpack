package scodec

import scodec.bits.{BitVector, ByteVector}

package object msgpack {

  import codecs.MessagePackCodec

  private def gen[A](s: Serialize[A]): Codec[A] = new Codec[A] {
    def encode(a: A): Attempt[BitVector] = s.pack(a).flatMap(MessagePackCodec.encode)
    def decode(buffer: BitVector): Attempt[DecodeResult[A]] =
      MessagePackCodec.decode(buffer).flatMap {
        case DecodeResult(a, rest) => s.unpack(a).map(DecodeResult(_, rest))
      }
    def sizeBound = MessagePackCodec.sizeBound
  }

  val bool: Codec[Boolean] = gen(Serialize.bool)
  val int: Codec[Int] = gen(Serialize.int)
  val long: Codec[Long] = gen(Serialize.long)
  val float: Codec[Float] = gen(Serialize.float)
  val double: Codec[Double] = gen(Serialize.double)
  val str: Codec[String] = gen(Serialize.string)
  val bin: Codec[ByteVector] = gen(Serialize.binary)

  def array[A : Serialize]: Codec[Vector[A]] = new Codec[Vector[A]] {
    def encode(a: Vector[A]): Attempt[BitVector] = Serialize.array.pack(a).flatMap(MessagePackCodec.encode)
    def decode(buffer: BitVector): Attempt[DecodeResult[Vector[A]]] =
      MessagePackCodec.decode(buffer).flatMap {
        case DecodeResult(a, rest) => Serialize.array.unpack(a).map(DecodeResult(_, rest))
      }
    def sizeBound = MessagePackCodec.sizeBound
  }

  def map[A : Serialize, B : Serialize]: Codec[Map[A, B]] = new Codec[Map[A, B]] {
    def encode(a: Map[A, B]): Attempt[BitVector] = Serialize.map[A, B].pack(a).flatMap(MessagePackCodec.encode)
    def decode(buffer: BitVector): Attempt[DecodeResult[Map[A, B]]] =
      MessagePackCodec.decode(buffer).flatMap {
        case DecodeResult(a, rest) => Serialize.map[A, B].unpack(a).map(DecodeResult(_, rest))
      }
    def sizeBound = MessagePackCodec.sizeBound
  }

  def extension[A : Codec](code: ByteVector): Codec[A] = gen(Serialize.extension(code))
}
