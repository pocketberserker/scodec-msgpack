package scodec
package msgpack

import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import org.scalatestplus.scalacheck.Checkers

class MessagePackSpecWithJava extends TestSuite with Checkers {

  def roundtripWithJava[A](a: A)(implicit C: WithJavaCodec[A]) = {
    roundtrip(a)(C.javaEncoderScalaDecoder)
    roundtrip(a)(C.scalaEncoderJavaDecoder)
  }

  "bool" should "be able to encode and decode" in {
    check(forAll((b: Boolean) => roundtripWithJava(b)))
  }

  "int" should "be able to encode and decode" in {
    check(forAll((a: Int) => roundtripWithJava(a)))
  }

  "long" should "be able to encode and decode" in {
    check(forAll((l: Long) => roundtripWithJava(l)))
  }

  "float" should "be able to encode and decode" in {
    check(forAll((f: Float) => roundtripWithJava(f)))
  }

  "double" should "be able to encode and decode" in {
    check(forAll((d: Double) => roundtripWithJava(d)))
  }

  "string" should "be able to encode and decode" in {
    check(forAll((s: String) => roundtripWithJava(s)))
  }

}
