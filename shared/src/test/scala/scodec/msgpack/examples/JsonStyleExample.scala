package scodec
package msgpack
package examples

import Serialize._

class JsonStyleExample extends TestSuite {

  sealed abstract class Setting extends Product with Serializable
  case class IsCompact(v: Boolean) extends Setting
  case class SchemaSize(v: Int) extends Setting

  implicit val settingSerializer: Serialize[Setting] = new Serialize[Setting] {
    def pack(v: Setting) = v match {
      case IsCompact(v) => Serialize.bool.pack(v)
      case SchemaSize(v) => Serialize.int.pack(v)
    }
    def unpack(v: MessagePack) =
      Serialize.bool.unpack(v).map(IsCompact)
        .orElse(Serialize.int.unpack(v).map(SchemaSize))
  }

  "json style data" should "encode and decode" in {
    val input = Map("compact" -> IsCompact(true), "schema" -> SchemaSize(0))
    roundtrip(input)
  }
}
