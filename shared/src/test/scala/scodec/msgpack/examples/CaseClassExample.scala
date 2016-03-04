package scodec
package msgpack
package examples

import Serialize._
import SerializeAuto._

sealed abstract class Setting extends Product with Serializable
final case class IsCompact(v: Boolean) extends Setting
final case class SchemaSize(v: Int) extends Setting
final case class MsgPackExample(compact: IsCompact, schema: SchemaSize)

class CaseClassExample extends TestSuite {

  "case class" should "encode and decode" in {
    val input = MsgPackExample(IsCompact(true), SchemaSize(0))
    roundtrip(input)
  }
}
