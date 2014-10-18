package scodec.msgpack

import scalaz._
import scalaz.std.option._
import scalaz.syntax.std.option._
import scalaz.std.vector._
import scalaz.syntax.std.map._
import scalaz.syntax.traverse._
import scodec.Codec
import scodec.bits.ByteVector

abstract class Serialize[A] {

  def pack(v: A): MessagePack
  def unpack(v: MessagePack): Option[A]
}

object Serialize {

  val bool: Serialize[Boolean] = new Serialize[Boolean] {

    def pack(v: Boolean): MessagePack = MBool(v)

    def unpack(v: MessagePack): Option[Boolean] = v match {
      case MBool(b) => b.some
      case _ => None
    }
  }

  val int: Serialize[Int] =new Serialize[Int] {

    def pack(v: Int): MessagePack =
      if(v >= 0 && v <= 127) MPositiveFixInt(v)
      else if(v >= 0 && v <= 255) MUInt8(v)
      else if(v >= 0 && v <= 65535) MUInt16(v)
      else if(v >= -32 && v <= -1) MNegativeFixInt(v)
      else if(v >= -128 && v < 0) MInt8(v)
      else if(v >= -32768 && v < 0) MInt16(v)
      else MInt32(v)

    def unpack(v: MessagePack): Option[Int] = v match {
      case MPositiveFixInt(n) => n.some
      case MNegativeFixInt(n) => n.some
      case MUInt8(n) => n.some
      case MUInt16(n) => n.some
      case MInt8(n) => n.some
      case MInt16(n) => n.some
      case MInt32(n) => n.some
      case _ => None
    }
  }

  val long: Serialize[Long] =new Serialize[Long] {

    def pack(v: Long): MessagePack =
      if(v >= 0L && v <= 4294967295L) MUInt32(v)
      else if(v >= 0L) MUInt64(v)
      else MInt64(v)

    def unpack(v: MessagePack): Option[Long] = v match {
      case MUInt32(n) => n.some
      case MUInt64(n) => n.some
      case MInt64(n) => n.some
      case _ => None
    }
  }

  val float: Serialize[Float] =new Serialize[Float] {

    def pack(v: Float): MessagePack = MFloat32(v)

    def unpack(v: MessagePack): Option[Float] = v match {
      case MFloat32(n) => n.some
      case _ => None
    }
  }

  val double: Serialize[Double] =new Serialize[Double] {

    def pack(v: Double): MessagePack = MFloat64(v)

    def unpack(v: MessagePack): Option[Double] = v match {
      case MFloat64(n) => n.some
      case _ => None
    }
  }

  val string: Serialize[String] =new Serialize[String] {
    def pack(v: String): MessagePack = {
      val len = v.size
      if(len <= 31) MFixString(v)
      else if(len <= 255) MString8(v)
      else if(len <= 65535) MString16(v)
      else MString32(v)
    }

    def unpack(v: MessagePack): Option[String] = v match {
      case MFixString(n) => n.some
      case MString8(n) => n.some
      case MString16(n) => n.some
      case MString32(n) => n.some
      case _ => None
    }
  }

  val binary: Serialize[ByteVector] =new Serialize[ByteVector] {
    def pack(v: ByteVector): MessagePack = {
      val len = v.size
      if(len <= 255) MBinary8(v)
      else if(len <= 65535) MBinary16(v)
      else MBinary32(v)
    }

    def unpack(v: MessagePack): Option[ByteVector] = v match {
      case MBinary8(n) => n.some
      case MBinary16(n) => n.some
      case MBinary32(n) => n.some
      case _ => None
    }
  }

  def array[A](implicit S: Serialize[A]): Serialize[Vector[A]] =new Serialize[Vector[A]] {
    def pack(v: Vector[A]): MessagePack = {
      val len = v.size
      val vm = v.map(S.pack)
      if(len <= 15) MFixArray(vm)
      else if(len <= 65535) MArray16(vm)
      else MArray32(vm)
    }

    def unpack(v: MessagePack): Option[Vector[A]] = v match {
      case MFixArray(n) => n.map(S.unpack).sequence
      case MArray16(n) => n.map(S.unpack).sequence
      case MArray32(n) => n.map(S.unpack).sequence
      case _ => None
    }
  }

  def map[A, B](implicit S: Serialize[A], T: Serialize[B]): Serialize[Map[A, B]] =new Serialize[Map[A, B]] {
    def pack(v: Map[A, B]): MessagePack = {
      val len = v.size
      val vm = v.mapValues(T.pack _).mapKeys(S.pack _)
      if(len <= 15) MFixMap(vm)
      else if(len <= 65535) MMap16(vm)
      else MMap32(vm)
    }

    def unpack(v: MessagePack): Option[Map[A, B]] = v match {
      case MFixMap(n) => n.toVector.map { case (k, v) => S.unpack(k).flatMap(kk => T.unpack(v).map((kk, _))) }.sequence.map(_.toMap)
      case MMap16(n) => n.toVector.map { case (k, v) => S.unpack(k).flatMap(kk => T.unpack(v).map((kk, _))) }.sequence.map(_.toMap)
      case MMap32(n) => n.toVector.map { case (k, v) => S.unpack(k).flatMap(kk => T.unpack(v).map((kk, _))) }.sequence.map(_.toMap)
      case _ => None
    }
  }

  def extended[A](code: ByteVector)(implicit S: Codec[A]): Serialize[A] =new Serialize[A] {
    def pack(v: A): MessagePack = {
      // TODO: use S.encode
      val encoded = S.encodeValid(v).bytes
      val len = encoded.size
      if(len <= 1) MFixExtended1(code, encoded)
      if(len <= 2) MFixExtended2(code, encoded)
      if(len <= 4) MFixExtended4(code, encoded)
      if(len <= 8) MFixExtended8(code, encoded)
      if(len <= 16) MFixExtended16(code, encoded)
      if(len <= 256) MExtended8(code, encoded)
      else if(len <= 65536) MExtended16(code, encoded)
      else MExtended32(code, encoded)
    }

    def unpack(v: MessagePack): Option[A] = v match {
      case MFixExtended1(_, value) => S.decodeValue(value.bits).toOption
      case MFixExtended2(_, value) => S.decodeValue(value.bits).toOption
      case MFixExtended4(_, value) => S.decodeValue(value.bits).toOption
      case MFixExtended8(_, value) => S.decodeValue(value.bits).toOption
      case MFixExtended16(_, value) => S.decodeValue(value.bits).toOption
      case MExtended8(_, value) => S.decodeValue(value.bits).toOption
      case MExtended16(_, value) => S.decodeValue(value.bits).toOption
      case MExtended32(_, value) => S.decodeValue(value.bits).toOption
      case _ => None
    }
  }
}
