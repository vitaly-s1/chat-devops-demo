name := "chat-client"
version := "0.1.0"
scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.9.6",
  "ch.qos.logback"                 % "logback-classic" % "1.4.14"
)

assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") =>
    MergeStrategy.discard
  case PathList("META-INF", xs @ _*) =>
    MergeStrategy.discard
  case x =>
    val old = (assemblyMergeStrategy in assembly).value
    old(x)
}

assembly / mainClass := Some("com.example.chat.client.ChatClient")
