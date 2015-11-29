package scodec.msgpack
package codecs

import scodec.bits.ByteVector

class MessagePackCodecSpec extends TestSuite {

  implicit val codec: scodec.Codec[MessagePack] = MessagePackCodec

  "nil" should "be able to encode and decode" in {
    roundtrip[MessagePack](MNil)
  }

  "bool" should "be able to encode and decode" in {
    roundtrip[MessagePack](MTrue)
    roundtrip[MessagePack](MFalse)
  }

  "positive fix int" should "be able to encode and decode" in {
    roundtrip[MessagePack](MPositiveFixInt(0))
    roundtrip[MessagePack](MPositiveFixInt(127))
  }

  "uint8" should "be able to encode and decode" in {
    roundtrip[MessagePack](MUInt8(0))
    roundtrip[MessagePack](MUInt8(255))
  }

  "uint16" should "be able to encode and decode" in {
    roundtrip[MessagePack](MUInt16(0))
    roundtrip[MessagePack](MUInt16(65535))
  }

  "uint32" should "be able to encode and decode" in {
    roundtrip[MessagePack](MUInt32(0))
    roundtrip[MessagePack](MUInt32(4294967295L))
  }

  "negative fix int" should "be able to encode and decode" in {
    roundtrip[MessagePack](MNegativeFixInt(-32))
    roundtrip[MessagePack](MNegativeFixInt(-1))
  }

  "int8" should "be able to encode and decode" in {
    roundtrip[MessagePack](MInt8(-128))
    roundtrip[MessagePack](MInt8(127))
  }

  "int16" should "be able to encode and decode" in {
    roundtrip[MessagePack](MInt16(-32768))
    roundtrip[MessagePack](MInt16(32767))
  }

  "int32" should "be able to encode and decode" in {
    roundtrip[MessagePack](MInt32(Int.MinValue))
    roundtrip[MessagePack](MInt32(Int.MaxValue))
  }

  "int64" should "be able to encode and decode" in {
    roundtrip[MessagePack](MInt64(Long.MinValue))
    roundtrip[MessagePack](MInt64(Long.MaxValue))
  }

  "float32" should "be able to encode and decode" in {
    roundtrip[MessagePack](MFloat32(Float.MinValue))
    roundtrip[MessagePack](MFloat32(Float.MaxValue))
  }

  "float64" should "be able to encode and decode" in {
    roundtrip[MessagePack](MFloat64(Double.MinValue))
    roundtrip[MessagePack](MFloat64(Double.MaxValue))
  }

  "fix str" should "be able to encode and decode" in {
    roundtrip[MessagePack](MFixString(""))
    roundtrip[MessagePack](MFixString("a" * 10))
  }

  "str8" should "be able to encode and decode" in {
    roundtrip[MessagePack](MString8(""))
    roundtrip[MessagePack](MString8("a" * 255))
  }

  "str16" should "be able to encode and decode" in {
    roundtrip[MessagePack](MString16(""))
    roundtrip[MessagePack](MString16("a" * 65535))
  }

  "str32" should "be able to encode and decode" in {
    roundtrip[MessagePack](MString32(""))
    roundtrip[MessagePack](MString32("a" * Long.MaxValue.toInt))
  }

  "bin8" should "be able to encode and decode" in {
    roundtrip[MessagePack](MBinary8(ByteVector.empty))
    roundtrip[MessagePack](MBinary8(ByteVector(0xa0)))
  }

  "bin16" should "be able to encode and decode" in {
    roundtrip[MessagePack](MBinary16(ByteVector.empty))
    roundtrip[MessagePack](MBinary16(ByteVector(0xff)))
  }

  "bin32" should "be able to encode and decode" in {
    roundtrip[MessagePack](MBinary32(ByteVector.empty))
    roundtrip[MessagePack](MBinary32(ByteVector(0x11)))
  }

  "fix array" should "be able to encode and decode" in {
    roundtrip[MessagePack](MFixArray(Vector.empty[MessagePack]))
    roundtrip[MessagePack](MFixArray(Vector(MInt8(127))))
  }

  "array16" should "be able to encode and decode" in {
    roundtrip[MessagePack](MArray16(Vector.empty[MessagePack]))
    roundtrip[MessagePack](MArray16(Vector(MInt8(127))))
  }

  "array32" should "be able to encode and decode" in {
    roundtrip[MessagePack](MArray32(Vector.empty[MessagePack]))
    roundtrip[MessagePack](MArray32(Vector(MInt8(127))))
  }

  "fix map" should "be able to encode and decode" in {
    roundtrip[MessagePack](MFixMap(Map.empty))
    roundtrip[MessagePack](MFixMap(Map(MFixString("a") -> MInt8(1))))
  }

  "map16" should "be able to encode and decode" in {
    roundtrip[MessagePack](MMap16(Map.empty))
    roundtrip[MessagePack](MMap16(Map(MFixString("a") -> MInt8(1))))
  }

  "map32" should "be able to encode and decode" in {
    roundtrip[MessagePack](MMap32(Map.empty))
    roundtrip[MessagePack](MMap32(Map(MFixString("a") -> MInt8(1))))
  }
}
