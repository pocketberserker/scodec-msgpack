package scodec.msgpack

import scodec.Codec
import Serialize._

trait WithJavaCodec[A]{
  def javaCodec: Codec[A]
  def scalaCodec: Codec[A]

  final def scalaEncoderJavaDecoder: Codec[A] =
    Codec(scalaCodec, javaCodec)

  final def javaEncoderScalaDecoder: Codec[A] =
    Codec(javaCodec, scalaCodec)
}

object WithJavaCodec {

  def apply[A](jcodec: Codec[A])(implicit scodec: Codec[A]): WithJavaCodec[A] =
    new WithJavaCodec[A] {
      override def scalaCodec = scodec
      override def javaCodec = jcodec
    }

  implicit val int: WithJavaCodec[Int] =
    apply(JavaCodec.int)

  implicit val long: WithJavaCodec[Long] =
    apply(JavaCodec.long)

  implicit val bool: WithJavaCodec[Boolean] =
    apply(JavaCodec.bool)

  implicit val float: WithJavaCodec[Float] =
    apply(JavaCodec.float)

  implicit val double: WithJavaCodec[Double] =
    apply(JavaCodec.double)

  implicit val str: WithJavaCodec[String] =
    apply(JavaCodec.str)
}
