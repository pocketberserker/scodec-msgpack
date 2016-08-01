import ScodecMsgPackBuild._

ScodecMsgPackBuild.buildSettings
publish := {}
publishLocal := {}
publishArtifact := false

lazy val msgpackJVM = msgpack.jvm
lazy val msgpackJS = msgpack.js
