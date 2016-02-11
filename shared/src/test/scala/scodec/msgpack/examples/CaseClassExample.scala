package scodec
package msgpack
package examples

import Serialize._
import SerializeAuto._

class CaseClassExample extends TestSuite {

  sealed abstract class Setting extends Product with Serializable
  case class IsCompact(v: Boolean) extends Setting
  case class SchemaSize(v: Int) extends Setting

  final case class MsgPackExample(compact: IsCompact, schema: SchemaSize)

  "case class" should "encode and decode" in {
    val input = MsgPackExample(IsCompact(true), SchemaSize(0))
    roundtrip(input)
  }
}
