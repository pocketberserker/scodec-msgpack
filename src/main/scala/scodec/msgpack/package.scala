package scodec

import scala.collection.immutable.IndexedSeq
import scalaz.\/
import scalaz.std.option._
import scalaz.syntax.std.option._
import scalaz.std.indexedSeq._
import scalaz.syntax.std.indexedSeq._
import scalaz.std.map._
import scalaz.syntax.std.map._
import scalaz.syntax.traverse._
import scodec.bits.{BitVector, ByteVector}

package object msgpack {

  import codecs.MessagePackCodec

  def fromBool(v: Boolean): MessagePack = MBool(v)

  def toBool(v: MessagePack): Option[Boolean] = v match {
    case MBool(b) => b.some
    case _ => None
  }

  def fromInt(v: Int): MessagePack =
    if(v >= 0 && v <= 127) MPositiveFixInt(v)
    else if(v >= 0 && v <= 255) MUInt8(v)
    else if(v >= 0 && v <= 65535) MUInt16(v)
    else if(v >= -32 && v <= -1) MNegativeFixInt(v)
    else if(v >= -128 && v < 0) MInt8(v)
    else if(v >= -32768 && v < 0) MInt16(v)
    else MInt32(v)

  def toInt(v: MessagePack): Option[Int] = v match {
    case MPositiveFixInt(n) => n.some
    case MNegativeFixInt(n) => n.some
    case MUInt8(n) => n.some
    case MUInt16(n) => n.some
    case MInt8(n) => n.some
    case MInt16(n) => n.some
    case MInt32(n) => n.some
    case _ => None
  }

  def fromLong(v: Long): MessagePack =
    if(v >= 0L && v <= 4294967295L) MUInt32(v)
    else if(v >= 0L) MUInt64(v)
    else MInt64(v)

  def toLong(v: MessagePack): Option[Long] = v match {
    case MUInt32(n) => n.some
    case MUInt64(n) => n.some
    case MInt64(n) => n.some
    case _ => None
  }

  def fromStr(v: String): MessagePack = {
    val len = v.size
    if(len <= 31) MFixString(v)
    else if(len <= 255) MString8(v)
    else if(len <= 65535) MString16(v)
    else MString32(v)
  }

  def toStr(v: MessagePack): Option[String] = v match {
    case MFixString(n) => n.some
    case MString8(n) => n.some
    case MString16(n) => n.some
    case MString32(n) => n.some
    case _ => None
  }

  def fromBinary(v: ByteVector): MessagePack = {
    val len = v.size
    if(len <= 255) MBinary8(v)
    else if(len <= 65535) MBinary16(v)
    else MBinary32(v)
  }

  def toBinary(v: MessagePack): Option[ByteVector] = v match {
    case MBinary8(n) => n.some
    case MBinary16(n) => n.some
    case MBinary32(n) => n.some
    case _ => None
  }

  private def gen[A](to: MessagePack => Option[A], from: A => MessagePack): Codec[A] = new Codec[A] {
    def encode(a: A): String \/ BitVector = MessagePackCodec.encode(from(a))
    def decode(buffer: BitVector): String \/ (BitVector, A) =
      MessagePackCodec.decode(buffer).flatMap { case (rest, a) => to(a).map((rest, _)).toRightDisjunction("fail to unapply") }
  }

  val bool: Codec[Boolean] = gen(toBool, fromBool)
  val int: Codec[Int] = gen(toInt, fromInt)
  val long: Codec[Long] = gen(toLong, fromLong)
  val str: Codec[String] = gen(toStr, fromStr)
  val bin: Codec[ByteVector] = gen(toBinary, fromBinary)

  private def genSeq[A](to: MessagePack => Option[IndexedSeq[A]], from: IndexedSeq[A] => MessagePack): Codec[IndexedSeq[A]] = new Codec[IndexedSeq[A]] {
    def encode(a: IndexedSeq[A]): String \/ BitVector = MessagePackCodec.encode(from(a))
    def decode(buffer: BitVector): String \/ (BitVector, IndexedSeq[A]) =
      MessagePackCodec.decode(buffer).flatMap { case (rest, a) => to(a).map((rest, _)).toRightDisjunction("fail to unapply") }
  }

  private def fromArray[A](v: IndexedSeq[A])(implicit f: A => MessagePack): MessagePack = {
    val len = v.size
    val vm = v.map(f)
    if(len <= 15) MFixArray(vm)
    else if(len <= 65535) MArray16(vm)
    else MArray32(vm)
  }

  private def toArray[A](v: MessagePack)(implicit f: MessagePack => Option[A]): Option[IndexedSeq[A]] = v match {
    case MFixArray(n) => n.map(f).sequence
    case MArray16(n) => n.map(f).sequence
    case MArray32(n) => n.map(f).sequence
    case _ => None
  }

  def array[A](implicit to: MessagePack => Option[A], from: A => MessagePack) = genSeq(toArray(_)(to), (v: IndexedSeq[A]) => fromArray(v)(from))

  private def genMap[A, B](to: MessagePack => Option[Map[A, B]], from: Map[A, B] => MessagePack): Codec[Map[A, B]] = new Codec[Map[A, B]] {
    def encode(a: Map[A, B]): String \/ BitVector = MessagePackCodec.encode(from(a))
    def decode(buffer: BitVector): String \/ (BitVector, Map[A, B]) =
      MessagePackCodec.decode(buffer).flatMap { case (rest, a) => to(a).map((rest, _)).toRightDisjunction("fail to unapply") }
  }

  private def fromMap[A, B](v: Map[A, B])(implicit fk: A => MessagePack, fv: B => MessagePack): MessagePack = {
    val len = v.size
    val vm = v.mapValues(fv).mapKeys(fk)
    if(len <= 15) MFixMap(vm)
    else if(len <= 65535) MMap16(vm)
    else MMap32(vm)
  }

  private def toMap[A, B](v: MessagePack)(implicit fk: MessagePack => Option[A], fv: MessagePack => Option[B]): Option[Map[A, B]] = v match {
    case MFixMap(n) => n.toIndexedSeq.map { case (k, v) => fk(k).flatMap(kk => fv(v).map((kk, _))) }.sequence.map(_.toMap)
    case MMap16(n) => n.toIndexedSeq.map { case (k, v) => fk(k).flatMap(kk => fv(v).map((kk, _))) }.sequence.map(_.toMap)
    case MMap32(n) => n.toIndexedSeq.map { case (k, v) => fk(k).flatMap(kk => fv(v).map((kk, _))) }.sequence.map(_.toMap)
    case _ => None
  }

  def map[A, B](implicit toKey: MessagePack => Option[A], fromKey: A => MessagePack, toValue: MessagePack => Option[B], fromValue: B => MessagePack) =
    genMap(toMap(_)(toKey, toValue), (v: Map[A, B]) => fromMap(v)(fromKey, fromValue))

  // TODO: extended type codec
}
