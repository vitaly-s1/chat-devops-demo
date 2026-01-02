package com.example.chat.client

import scala.io.StdIn
import scala.concurrent.Await
import scala.concurrent.duration._
import sttp.client3._
import sttp.model.Uri

object ChatClient {

  case class Config(
    server: String = "http://localhost:8080",
    listRooms: Boolean = false,
    room: Option[String] = None,
    user: Option[String] = None
  )

  def parseArgs(args: Array[String]): Config = {
    var cfg = Config()
    args.sliding(1,1).foreach {
      case Array(arg) if arg.startsWith("--server=") =>
        cfg = cfg.copy(server = arg.stripPrefix("--server="))
      case Array("--list-rooms") =>
        cfg = cfg.copy(listRooms = true)
      case Array(arg) if arg.startsWith("--room=") =>
        cfg = cfg.copy(room = Some(arg.stripPrefix("--room=")))
      case Array(arg) if arg.startsWith("--user=") =>
        cfg = cfg.copy(user = Some(arg.stripPrefix("--user=")))
      case _ =>
    }
    cfg
  }

  def discoveryMode(cfg: Config): Unit = {
    implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()
    val url = uri"${cfg.server}/api/rooms/stats"

    val resp = basicRequest.get(url).send(backend)
    println(s"Response code: ${resp.code}")
    println("Body:")
    println(resp.body.merge)
  }

  def interactiveMode(cfg: Config): Unit = {
    implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()
    val room = cfg.room.get
    val user = cfg.user.get
    val base = cfg.server

    println(s"Joined room '$room' as '$user'. Type messages, /quit to exit.")

    var running = true
    while (running) {
      val line = StdIn.readLine("> ")
      if (line == null || line == "/quit") {
        running = false
      } else if (line.trim.nonEmpty) {
        val url = uri"$base/api/rooms/$room/message?user=$user&text=$line"
        val resp = basicRequest.post(url).send(backend)
        if (!resp.code.isSuccess) {
          println(s"Failed to send message: ${resp.code}")
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val cfg = parseArgs(args)

    if (cfg.listRooms) {
      discoveryMode(cfg)
    } else {
      (cfg.room, cfg.user) match {
        case (Some(_), Some(_)) => interactiveMode(cfg)
        case _ =>
          println("Usage:")
          println("  chat-client --server=<SERVER_ADDRESS> --list-rooms")
          println("  chat-client --server=<SERVER_ADDRESS> --room=<ROOM_ID> --user=<USER_NAME>")
      }
    }
  }
}
