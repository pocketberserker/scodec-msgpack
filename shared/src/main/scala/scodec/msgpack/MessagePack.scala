package scodec.msgpack

import scodec.bits.ByteVector

sealed abstract class MessagePack
case class MPositiveFixInt(i: Int) extends MessagePack
case class MFixMap(m: Map[MessagePack, MessagePack]) extends MessagePack
case class MFixArray(a: Vector[MessagePack]) extends MessagePack
case class MFixString(s: String) extends MessagePack
case object MNil extends MessagePack
sealed abstract class MBool extends MessagePack {
  def value: Boolean
}
case object MFalse extends MBool {
  val value = false
}
case object MTrue extends MBool {
  val value = true
}
case class MBinary8(b: ByteVector) extends MessagePack
case class MBinary16(b: ByteVector) extends MessagePack
case class MBinary32(b: ByteVector) extends MessagePack
case class MExtension8(size: Int, code: ByteVector, data: ByteVector) extends MessagePack
case class MExtension16(size: Int, code: ByteVector, data: ByteVector) extends MessagePack
case class MExtension32(size: Long, code: ByteVector, data: ByteVector) extends MessagePack
case class MFloat32(f: Float) extends MessagePack
case class MFloat64(d: Double) extends MessagePack
case class MUInt8(i: Int) extends MessagePack
case class MUInt16(i: Int) extends MessagePack
case class MUInt32(i: Long) extends MessagePack
case class MUInt64(i: Long) extends MessagePack
case class MInt8(i: Int) extends MessagePack
case class MInt16(i: Int) extends MessagePack
case class MInt32(i: Int) extends MessagePack
case class MInt64(i: Long) extends MessagePack
case class MFixExtension1(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtension2(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtension4(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtension8(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtension16(code: ByteVector, data: ByteVector) extends MessagePack
case class MString8(s: String) extends MessagePack
case class MString16(s: String) extends MessagePack
case class MString32(s: String) extends MessagePack
case class MArray16(a: Vector[MessagePack]) extends MessagePack
case class MArray32(a: Vector[MessagePack]) extends MessagePack
case class MMap16(m: Map[MessagePack, MessagePack]) extends MessagePack
case class MMap32(m: Map[MessagePack, MessagePack]) extends MessagePack
case class MNegativeFixInt(i: Int) extends MessagePack

