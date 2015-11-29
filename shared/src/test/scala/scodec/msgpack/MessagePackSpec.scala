package scodec
package msgpack

import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import org.scalatest.prop.Checkers

class MessagePackSpec extends TestSuite with Checkers {

  import Serialize._

  "bool" should "be able to encode and decode" in {
    check(forAll((b: Boolean) => roundtrip(b)))
  }

  "int" should "be able to encode and decode" in {
    check(forAll((a: Int) => roundtrip(a)))
  }

  "long" should "be able to encode and decode" in {
    check(forAll((l: Long) => roundtrip(l)))
  }

  "float" should "be able to encode and decode" in {
    check(forAll((f: Float) => roundtrip(f)))
  }

  "double" should "be able to encode and decode" in {
    check(forAll((d: Double) => roundtrip(d)))
  }

  "string" should "be able to encode and decode" in {
    check(forAll((s: String) => roundtrip(s)))
  }

  "array" should "be able to encode and decode" in {
    check(forAll((a: Vector[Int]) => roundtrip(a)))
  }

  "map" should "be able to encode and decode" in {
    check(forAll((m: Map[Int, Int]) => roundtrip(m)))
  }

  case class Point(x: Int, y: Int, z: Int)

  "Point" should "be able to encode and decode" in {
    roundtrip(Point(1000, 5, 2))
  }
}
