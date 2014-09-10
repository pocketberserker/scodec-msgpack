package scodec.msgpack
package codecs

import scalaz.NonEmptyList
import scodec.Codec
import scodec.bits.{BitVector, ByteVector}
import scodec.bits._
import scodec.codecs._

object MessagePackCodec extends Codec[MessagePack] {

  private def fixValue[A](b: Codec[BitVector], head: BitVector, body: Codec[A],
    apply: A => MessagePack, unapply: MessagePack => Option[A], name: String): Codec[MessagePack] =
    new FixValueCodec(b, head, body, apply, unapply, name)

  private def unapplyPositiveFixInt(m: MessagePack): Option[Int] = m match {
    case MPositiveFixInt(i) => Some(i)
    case _ => None
  }

  private val positiveFixInt = fixValue(bits(1), bin"0", uint(7), MPositiveFixInt.apply, unapplyPositiveFixInt, "positive fix int")

  private def unapplyNegativeFixInt(m: MessagePack): Option[Int] = m match {
    case MNegativeFixInt(i) => Some(i)
    case _ => None
  }

  private val negativeFixInt =
    fixValue(bits(3), bin"111", uint(5).xmap(_ - 0x20, (a: Int) => a + 0x20), MNegativeFixInt.apply, unapplyNegativeFixInt, "negative fix int")

  private def unapplyFixString(m: MessagePack): Option[String] = m match {
    case MFixString(i) => Some(i)
    case _ => None
  }

  private val fixStr = fixValue(bits(3), bin"101", variableSizeBytes(uint(5), utf8), MFixString.apply, unapplyFixString, "fix str")

  private def variableSizeBytesL[A](size: Codec[Long], value: Codec[A]): Codec[A] =
    new VariableLongSizeBytesCodec(size, value)

  private val str8 = variableSizeBytes(uint8, utf8)
  private val str16 = variableSizeBytes(uint16, utf8)
  private val str32 = variableSizeBytesL(uint32, utf8)

  private val bin8 = variableSizeBytes(uint8, bytes)
  private val bin16 = variableSizeBytes(uint16, bytes)
  private val bin32 = variableSizeBytesL(uint32, bytes)

  private def unapplyFixArray(m: MessagePack): Option[Vector[MessagePack]] = m match {
    case MFixArray(a) => Some(a)
    case _ => None
  }

  private def fixArray: Codec[MessagePack] =
    fixValue(bits(4),  bin"1001", array(uint(4)), MFixArray.apply, unapplyFixArray, "fix aaray")

  private def array(size: Codec[Int]): Codec[Vector[MessagePack]] =
    new ArrayCodec(size)

  private val array16 = array(uint16)

  private def longArray(size: Codec[Long]): Codec[Vector[MessagePack]] =
    new LongArrayCodec(size)

  private val array32 = longArray(uint32)

  private def unapplyFixMap(m: MessagePack): Option[Map[MessagePack, MessagePack]] = m match {
    case MFixMap(m) => Some(m)
    case _ => None
  }

  private val fixMap: Codec[MessagePack] =
    fixValue(bits(4),  bin"1000", mmap(uint(4)), MFixMap.apply, unapplyFixMap, "fix map")

  private def mmap(size: Codec[Int]): Codec[Map[MessagePack, MessagePack]] =
    new MapCodec(size)

  private val map16 = mmap(uint16)

  private def longMap(size: Codec[Long]): Codec[Map[MessagePack, MessagePack]] =
    new LongMapCodec(size)

  private val map32 = longMap(uint32)

  private val fixExt1 = bytes(1) ~ bytes(1)
  private val fixExt2 = bytes(1) ~ bytes(2)
  private val fixExt4 = bytes(1) ~ bytes(4)
  private val fixExt8 = bytes(1) ~ bytes(8)
  private val fixExt16 = bytes(1) ~ bytes(16)

  private def extended(size: Codec[Int]): Codec[(ByteVector, ByteVector)] = new ExtendedValueCodec(size)

  private val ext8 = extended(uint8)
  private val ext16 = extended(uint16)
  private val ext32 = new LongExtendedValueCodec(uint32)

  private val byteHead: Codec[MessagePack] =
    discriminated[MessagePack].by(bytes(1))
    .\ (ByteVector(0xc0)) { case n : MNil.type => n } (provide(MNil))
    .\ (ByteVector(0xc2)) { case b @ MBool(false) => b } (provide(MBool(false)))
    .\ (ByteVector(0xc3)) { case b @ MBool(true) => b } (provide(MBool(true)))
    .| (ByteVector(0xc4)) { case MBinary8(b) => b } (MBinary8.apply) (bin8)
    .| (ByteVector(0xc5)) { case MBinary16(b) => b } (MBinary16.apply) (bin16)
    .| (ByteVector(0xc6)) { case MBinary32(b) => b } (MBinary32.apply) (bin32)
    .| (ByteVector(0xc7)) { case MExtended8(c, d) => (c, d) } (MExtended8.apply) (ext8)
    .| (ByteVector(0xc8)) { case MExtended16(c, d) => (c, d) } (MExtended16.apply) (ext16)
    .| (ByteVector(0xc9)) { case MExtended32(c, d) => (c, d) } (MExtended32.apply) (ext32)
    .| (ByteVector(0xca)) { case MFloat32(n) => n } (MFloat32.apply) (float)
    .| (ByteVector(0xcb)) { case MFloat64(n) => n } (MFloat64.apply) (double)
    .| (ByteVector(0xcc)) { case MUInt8(n) => n } (MUInt8.apply) (uint8)
    .| (ByteVector(0xcd)) { case MUInt16(n) => n } (MUInt16.apply) (uint16)
    .| (ByteVector(0xce)) { case MUInt32(n) => n } (MUInt32.apply) (uint32)
    .| (ByteVector(0xcf)) { case MUInt64(n) => n } (MUInt64.apply) (ulong(63))
    .| (ByteVector(0xd0)) { case MInt8(n) => n } (MInt8.apply) (int8)
    .| (ByteVector(0xd1)) { case MInt16(n) => n } (MInt16.apply) (int16)
    .| (ByteVector(0xd2)) { case MInt32(n) => n } (MInt32.apply) (int32)
    .| (ByteVector(0xd3)) { case MInt64(n) => n } (MInt64.apply) (int64)
    .| (ByteVector(0xd4)) { case MFixExtended1(c, d) => (c, d) } (MFixExtended1.apply) (fixExt1)
    .| (ByteVector(0xd5)) { case MFixExtended2(c, d) => (c, d) } (MFixExtended2.apply) (fixExt2)
    .| (ByteVector(0xd6)) { case MFixExtended4(c, d) => (c, d) } (MFixExtended4.apply) (fixExt4)
    .| (ByteVector(0xd7)) { case MFixExtended8(c, d) => (c, d) } (MFixExtended8.apply) (fixExt8)
    .| (ByteVector(0xd8)) { case MFixExtended16(c, d) => (c, d) } (MFixExtended16.apply) (fixExt16)
    .| (ByteVector(0xd9)) { case MString8(s) => s } (MString8.apply) (str8)
    .| (ByteVector(0xda)) { case MString16(s) => s } (MString16.apply) (str16)
    .| (ByteVector(0xdb)) { case MString32(s) => s } (MString32.apply) (str32)
    .| (ByteVector(0xdc)) { case MArray16(a) => a } (MArray16.apply) (array16)
    .| (ByteVector(0xdd)) { case MArray32(a) => a } (MArray32.apply) (array32)
    .| (ByteVector(0xde)) { case MMap16(m) => m } (MMap16.apply) (map16)
    .| (ByteVector(0xdf)) { case MMap32(m) => m } (MMap32.apply) (map32)

  private val msgpack = new ChoiceCodec(NonEmptyList(
    positiveFixInt,
    negativeFixInt,
    fixStr,
    fixArray,
    fixMap,
    byteHead))

  override def encode(m: MessagePack) = msgpack.encode(m)

  override def decode(m: BitVector) = msgpack.decode(m)
}
