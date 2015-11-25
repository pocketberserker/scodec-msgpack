package scodec.msgpack

import scala.util.control.NonFatal
import java.io.ByteArrayOutputStream
import org.msgpack.core.{MessagePack => JMessagePack, MessagePacker, MessageUnpacker}
import scodec._
import scodec.bits.BitVector

object JavaCodec {

  val bitVector2Unpacker: BitVector => MessageUnpacker = { bits =>
    JMessagePack.newDefaultUnpacker(bits.toByteArray)
  }

  def withPacker(f: MessagePacker => Unit): Attempt[BitVector] = {
    val out = new ByteArrayOutputStream()
    val packer = JMessagePack.newDefaultPacker(out)
    f(packer)
    packer.close
    Attempt.successful(BitVector(out.toByteArray))
  }

  def fromTryCatchNonFatal[A](a: => DecodeResult[A]): Attempt[DecodeResult[A]] = try {
    Attempt.successful(a)
  } catch {
    case NonFatal(t) => Attempt.failure(Err.apply(t.toString))
  }

  def withUnpacker[A](f: MessageUnpacker => A): BitVector => Attempt[DecodeResult[A]] =
    bitVector2Unpacker.andThen { unpacker =>
      val result = fromTryCatchNonFatal(
        DecodeResult(f(unpacker), BitVector.empty)
      )
      unpacker.close()
      result
    }

  def javacodec[A](encoder: (MessagePacker, A) => Unit, decoder: MessageUnpacker => A): Codec[A] =
    Codec(
      v => withPacker(packer => encoder(packer, v)),
      withUnpacker(decoder)
    )

  val int: Codec[Int] = javacodec(
    _ packInt _,
    _.unpackInt
  )

  val long: Codec[Long] = javacodec(
    _ packLong _,
    _.unpackLong
  )

  val bool: Codec[Boolean] = javacodec(
    _ packBoolean _,
    _.unpackBoolean
  )

  val double: Codec[Double] = javacodec(
    _ packDouble _,
    _.unpackDouble
  )

  val float: Codec[Float] = javacodec(
    _ packFloat  _,
    _.unpackFloat
  )

  val str: Codec[String] = javacodec(
    _ packString _,
    _.unpackString
  )
}
