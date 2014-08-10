package scodec.msgpack

import scala.collection.immutable.IndexedSeq
import scodec.bits.ByteVector

sealed abstract class MessagePack

case object MNil extends MessagePack
case class MBool(b: Boolean) extends MessagePack
case class MPositiveFixInt(i: Int) extends MessagePack
case class MNegativeFixInt(i: Int) extends MessagePack
case class MUInt8(i: Int) extends MessagePack
case class MUInt16(i: Int) extends MessagePack
case class MUInt32(i: Long) extends MessagePack
case class MUInt64(i: Long) extends MessagePack
case class MInt8(i: Int) extends MessagePack
case class MInt16(i: Int) extends MessagePack
case class MInt32(i: Int) extends MessagePack
case class MInt64(i: Long) extends MessagePack
case class MFloat32(f: Float) extends MessagePack
case class MFloat64(d: Double) extends MessagePack
case class MFixString(s: String) extends MessagePack
case class MString8(s: String) extends MessagePack
case class MString16(s: String) extends MessagePack
case class MString32(s: String) extends MessagePack
case class MBinary8(b: ByteVector) extends MessagePack
case class MBinary16(b: ByteVector) extends MessagePack
case class MBinary32(b: ByteVector) extends MessagePack
case class MFixArray(a: IndexedSeq[MessagePack]) extends MessagePack
case class MArray16(a: IndexedSeq[MessagePack]) extends MessagePack
case class MArray32(a: IndexedSeq[MessagePack]) extends MessagePack
case class MFixMap(m: Map[MessagePack, MessagePack]) extends MessagePack
case class MMap16(m: Map[MessagePack, MessagePack]) extends MessagePack
case class MMap32(m: Map[MessagePack, MessagePack]) extends MessagePack
case class MFixExtended1(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtended2(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtended4(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtended8(code: ByteVector, data: ByteVector) extends MessagePack
case class MFixExtended16(code: ByteVector, data: ByteVector) extends MessagePack
case class MExtended8(code: ByteVector, data: ByteVector) extends MessagePack
case class MExtended16(code: ByteVector, data: ByteVector) extends MessagePack
case class MExtended32(code: ByteVector, data: ByteVector) extends MessagePack


