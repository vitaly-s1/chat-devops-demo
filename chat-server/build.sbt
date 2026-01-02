name := "chat-server"
version := "0.1.0"
scalaVersion := "2.13.14"

val akkaVersion = "2.6.21" 
val akkaHttpVersion = "10.2.10" 

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "ch.qos.logback"     % "logback-classic"  % "1.4.14"
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

assembly / mainClass := Some("com.example.chat.server.HttpServer")
