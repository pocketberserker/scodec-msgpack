package scodec.msgpack

import org.scalacheck._
import Arbitrary.arbitrary
import scodec.bits.ByteVector

// TODO: utf8 string arbitrary
object MessagePackArbitrary {

  lazy val codepoint1: Arbitrary[ByteVector] = Arbitrary(
    Gen.chooseNum(0, 0x7f).map(ByteVector(_))
  )

  implicit lazy val genASCII: Gen[String] =
    arbitrary(codepoint1).map(v => new String(v.toArray))
}
