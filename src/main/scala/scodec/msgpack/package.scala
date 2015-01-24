package scodec

import scodec.bits.{BitVector, ByteVector}

package object msgpack {

  import codecs.MessagePackCodec

  private def gen[A](s: Serialize[A]): Codec[A] = new Codec[A] {
    def encode(a: A): Attempt[BitVector] = MessagePackCodec.encode(s.pack(a))
    def decode(buffer: BitVector): Attempt[DecodeResult[A]] =
      MessagePackCodec.decode(buffer).flatMap {
        case DecodeResult(a, rest) =>
          Attempt.fromOption(
            s.unpack(a).map(DecodeResult(_, rest)), Err("fail to unpack"))
      }
  }

  val bool: Codec[Boolean] = gen(Serialize.bool)
  val int: Codec[Int] = gen(Serialize.int)
  val long: Codec[Long] = gen(Serialize.long)
  val float: Codec[Float] = gen(Serialize.float)
  val double: Codec[Double] = gen(Serialize.double)
  val str: Codec[String] = gen(Serialize.string)
  val bin: Codec[ByteVector] = gen(Serialize.binary)

  def array[A : Serialize]: Codec[Vector[A]] = new Codec[Vector[A]] {
    def encode(a: Vector[A]): Attempt[BitVector] = MessagePackCodec.encode(Serialize.array.pack(a))
    def decode(buffer: BitVector): Attempt[DecodeResult[Vector[A]]] =
      MessagePackCodec.decode(buffer).flatMap {
        case DecodeResult(a, rest) =>
          Attempt.fromOption(
            Serialize.array.unpack(a).map(DecodeResult(_, rest)), Err("fail to unpack"))
      }
  }

  def map[A : Serialize, B : Serialize]: Codec[Map[A, B]] = new Codec[Map[A, B]] {
    def encode(a: Map[A, B]): Attempt[BitVector] = MessagePackCodec.encode(Serialize.map[A, B].pack(a))
    def decode(buffer: BitVector): Attempt[DecodeResult[Map[A, B]]] =
      MessagePackCodec.decode(buffer).flatMap {
        case DecodeResult(a, rest) =>
          Attempt.fromOption(
            Serialize.map[A, B].unpack(a).map(DecodeResult(_, rest)), Err("fail to unpack"))
      }
  }

  def extended[A : Codec](code: ByteVector): Codec[A] = gen(Serialize.extended(code))
}
