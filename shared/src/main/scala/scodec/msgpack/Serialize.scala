package scodec.msgpack

import scodec.{Err, Attempt, Codec}
import scodec.bits.ByteVector

abstract class Serialize[A] { self =>

  def pack(v: A): Attempt[MessagePack]
  def unpack(v: MessagePack): Attempt[A]

  final def xmap[B](f: A => B, g: B => A): Serialize[B] = new Serialize[B] {
    override def pack(v: B) = self.pack(g(v))
    override def unpack(v: MessagePack) = self.unpack(v).map(f)
  }
}

object Serialize {

  private def fail(v: String) = Attempt.failure(Err(s"fail to unpack: $v"))

  val bool: Serialize[Boolean] = new Serialize[Boolean] {

    def pack(v: Boolean): Attempt[MessagePack] = Attempt.successful(if (v) MTrue else MFalse)

    def unpack(v: MessagePack): Attempt[Boolean] = v match {
      case m : MBool => Attempt.successful(m.value)
      case _ => fail("Boolean")
    }
  }

  val int: Serialize[Int] = new Serialize[Int] {

    // This implementation refer to msgpack-java (Apache License version 2.0).
    // https://github.com/msgpack/msgpack-java/blob/bd9b4f20597111775120546de41dd3f9d01b9616/msgpack-core/src/main/java/org/msgpack/core/MessagePacker.java#L253
    // Change log: fix indent and return values.
    def pack(v: Int): Attempt[MessagePack] = Attempt.successful(
      if (v < -(1 << 5)) {
        if (v < -(1 << 15)) MInt32(v)
        else if (v < -(1 << 7)) MInt16(v)
        else MInt8(v)
      }
      else if (v < (1 << 7)) {
        if (v >= 0) MPositiveFixInt(v)
        else MNegativeFixInt(v)
      }
      else {
        if (v < (1 << 8)) MUInt8(v)
        else if (v < (1 << 16)) MUInt16(v)
        else MUInt32(v)
      })

    def unpack(v: MessagePack): Attempt[Int] = v match {
      case MPositiveFixInt(n) => Attempt.successful(n)
      case MNegativeFixInt(n) => Attempt.successful(n)
      case MUInt8(n) => Attempt.successful(n)
      case MUInt16(n) => Attempt.successful(n)
      case MUInt32(n) => Attempt.successful(n.toInt)
      case MInt8(n) => Attempt.successful(n)
      case MInt16(n) => Attempt.successful(n)
      case MInt32(n) => Attempt.successful(n)
      case _ => fail("Int")
    }
  }

  val long: Serialize[Long] = new Serialize[Long] {

    // This implementation refer to msgpack-java (Apache License version 2.0).
    // https://github.com/msgpack/msgpack-java/blob/bd9b4f20597111775120546de41dd3f9d01b9616/msgpack-core/src/main/java/org/msgpack/core/MessagePacker.java#L277
    // Change log: fix indent and return values.
    def pack(v: Long): Attempt[MessagePack] = Attempt.successful(
      if (v < -(1L << 5)) {
        if (v < -(1L << 15)) {
          if (v < -(1L << 31)) MInt64(v)
          else  MInt32(v.toInt)
        }
        else {
          if (v < -(1 << 7)) MInt16(v.toInt)
          else MInt8(v.toInt)
        }
      }
      else if (v < (1 << 7)) {
        if (v >= 0) MPositiveFixInt(v.toInt)
        else MNegativeFixInt(v.toInt)
      }
      else {
        if (v < (1L << 16)) {
          if (v < (1 << 8)) MUInt8(v.toInt)
          else MUInt16(v.toInt)
        }
        else {
          if (v < (1L << 32)) MUInt32(v.toInt)
          else MUInt64(v)
        }
      })

    def unpack(v: MessagePack): Attempt[Long] = v match {
      case MPositiveFixInt(n) => Attempt.successful(n.toLong)
      case MNegativeFixInt(n) => Attempt.successful(n.toLong)
      case MUInt8(n) => Attempt.successful(n.toLong)
      case MUInt16(n) => Attempt.successful(n.toLong)
      case MUInt32(n) => Attempt.successful(n)
      case MUInt64(n) => Attempt.successful(n)
      case MInt8(n) => Attempt.successful(n.toLong)
      case MInt16(n) => Attempt.successful(n.toLong)
      case MInt32(n) => Attempt.successful(n.toLong)
      case MInt64(n) => Attempt.successful(n)
      case _ => fail("Long")
    }
  }

  val float: Serialize[Float] = new Serialize[Float] {

    def pack(v: Float): Attempt[MessagePack] = Attempt.successful(MFloat32(v))

    def unpack(v: MessagePack): Attempt[Float] = v match {
      case MFloat32(n) => Attempt.successful(n)
      case _ => fail("Float")
    }
  }

  val double: Serialize[Double] = new Serialize[Double] {

    def pack(v: Double): Attempt[MessagePack] = Attempt.successful(MFloat64(v))

    def unpack(v: MessagePack): Attempt[Double] = v match {
      case MFloat64(n) => Attempt.successful(n)
      case _ => fail("Double")
    }
  }

  val string: Serialize[String] = new Serialize[String] {
    def pack(v: String): Attempt[MessagePack] = Attempt.successful{
      val len = v.getBytes("UTF-8").length
      if(len <= 31) MFixString(v)
      else if(len <= 255) MString8(v)
      else if(len <= 65535) MString16(v)
      else MString32(v)
    }

    def unpack(v: MessagePack): Attempt[String] = v match {
      case MFixString(n) => Attempt.successful(n)
      case MString8(n) => Attempt.successful(n)
      case MString16(n) => Attempt.successful(n)
      case MString32(n) => Attempt.successful(n)
      case _ => fail("String")
    }
  }

  val binary: Serialize[ByteVector] = new Serialize[ByteVector] {
    def pack(v: ByteVector): Attempt[MessagePack] = Attempt.successful{
      val len = v.size
      if(len <= 255) MBinary8(v)
      else if(len <= 65535) MBinary16(v)
      else MBinary32(v)
    }

    def unpack(v: MessagePack): Attempt[ByteVector] = v match {
      case MBinary8(n) => Attempt.successful(n)
      case MBinary16(n) => Attempt.successful(n)
      case MBinary32(n) => Attempt.successful(n)
      case _ => fail("ByteVector")
    }
  }

  def array[A](implicit S: Serialize[A]): Serialize[Vector[A]] = new Serialize[Vector[A]] {
    def pack(vec: Vector[A]): Attempt[MessagePack] = {
      val len = vec.size
      vec.foldLeft(Attempt.successful(Vector.empty[MessagePack])){
        case (acc, v) => acc.flatMap(a => S.pack(v).map(a :+ _))
      } .map(vm =>
        if(len <= 15) MFixArray(vm)
        else if(len <= 65535) MArray16(vm)
        else MArray32(vm)
      )
    }

    private def sequence(vec: Vector[MessagePack]): Attempt[Vector[A]] =
      vec.foldLeft(Attempt.successful(Vector.empty[A])){ case (acc, v) => acc.flatMap(a => S.unpack(v).map(a :+ _))}

    def unpack(v: MessagePack): Attempt[Vector[A]] = v match {
      case MFixArray(n) => sequence(n)
      case MArray16(n) => sequence(n)
      case MArray32(n) => sequence(n)
      case _ => fail("Array")
    }
  }

  def map[A, B](implicit S: Serialize[A], T: Serialize[B]): Serialize[Map[A, B]] = new Serialize[Map[A, B]] {
    def pack(m: Map[A, B]): Attempt[MessagePack] = {
      val len = m.size
      m.toVector.foldLeft(Attempt.successful(Vector.empty[(MessagePack, MessagePack)])){
        case (acc, (k, v)) => acc.flatMap(a => S.pack(k).flatMap(kk => T.pack(v).map(vv => a :+ ((kk, vv)))))
      } map{vm =>
        val mm = vm.toMap
        if(len <= 15) MFixMap(mm)
        else if(len <= 65535) MMap16(mm)
        else MMap32(mm)
      }
    }

    private def sequence(m: Map[MessagePack, MessagePack]): Attempt[Map[A, B]] =
      m.toVector.foldLeft(Attempt.successful(Vector.empty[(A, B)])){
        case (acc, (k, v)) => acc.flatMap(a => S.unpack(k).flatMap(kk => T.unpack(v).map(vv => a :+ ((kk, vv)))))
      }.map(_.toMap)

    def unpack(v: MessagePack): Attempt[Map[A, B]] = v match {
      case MFixMap(n) => sequence(n)
      case MMap16(n) => sequence(n)
      case MMap32(n) => sequence(n)
      case _ => fail("Map")
    }
  }

  def extension[A](code: ByteVector)(implicit S: Codec[A]): Serialize[A] = new Serialize[A] {
    def pack(v: A): Attempt[MessagePack] =
      S.encode(v).map(_.bytes).map{encoded =>
        val len = encoded.size
        if(len <= 1) MFixExtension1(code, encoded)
        if(len <= 2) MFixExtension2(code, encoded)
        if(len <= 4) MFixExtension4(code, encoded)
        if(len <= 8) MFixExtension8(code, encoded)
        if(len <= 16) MFixExtension16(code, encoded)
        if(len <= 256) MExtension8(len, code, encoded)
        else if(len <= 65536) MExtension16(len, code, encoded)
        else MExtension32(len.toLong, code, encoded)
      }

    def unpack(v: MessagePack): Attempt[A] = v match {
      case MFixExtension1(_, value) => S.decode(value.bits).map(_.value)
      case MFixExtension2(_, value) => S.decode(value.bits).map(_.value)
      case MFixExtension4(_, value) => S.decode(value.bits).map(_.value)
      case MFixExtension8(_, value) => S.decode(value.bits).map(_.value)
      case MFixExtension16(_, value) => S.decode(value.bits).map(_.value)
      case MExtension8(_, _, value) => S.decode(value.bits).map(_.value)
      case MExtension16(_, _, value) => S.decode(value.bits).map(_.value)
      case MExtension32(_, _, value) => S.decode(value.bits).map(_.value)
      case _ => fail("Extension")
    }
  }
}
