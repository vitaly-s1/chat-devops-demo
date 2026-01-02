package com.example.chat.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.typed.scaladsl.AskPattern._

object HttpServer {

  def start(instanceId: String)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    implicit val timeout: Timeout = 3.seconds

    val roomRegistry = system.systemActorOf(RoomRegistry(), "room-registry")

    val routes =
      path("metrics") {
        get {
          complete(ChatMetrics.toPrometheusText(instanceId))
        }
      } ~
      pathPrefix("api") {
        concat(
          path("rooms" / "stats") {
            get {
              val statsF: Future[RoomRegistry.Stats] =
                roomRegistry.ask(RoomRegistry.GetStats)
              onSuccess(statsF) { stats =>
                val json =
                  s"""
                     |{
                     |  "activeRooms": ${stats.activeRooms},
                     |  "totalMessages": ${stats.totalMessages}
                     |}
                     |""".stripMargin
                complete(json)
              }
            }
          },
          path("rooms" / Segment / "message") { roomId =>
            post {
              parameters("user".as[String], "text".as[String]) { (user, text) =>
                roomRegistry ! RoomRegistry.IncomingMessage(roomId, user, text)
                complete("ok")
              }
            }
          }
        )
      }

    val bindAddress = sys.env.getOrElse("CHAT_SERVER_BIND_ADDRESS", "0.0.0.0")
    val bindPort    = sys.env.getOrElse("CHAT_SERVER_PORT", "8080").toInt

    Http().newServerAt(bindAddress, bindPort).bind(routes).foreach { binding =>
      system.log.info(s"HTTP server online at http://$bindAddress:${binding.localAddress.getPort}/")
    }
  }

  def main(args: Array[String]): Unit = {
    val instanceId = sys.env.getOrElse("CHAT_INSTANCE_ID", java.util.UUID.randomUUID().toString)

    implicit val system: ActorSystem[Nothing] =
      ActorSystem[Nothing](Behaviors.empty, "chat-server-system")

    start(instanceId)
  }
}
