package com.example.chat.server

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors}

object RoomRegistry {

  sealed trait Command
  final case class JoinRoom(roomId: String, user: String, replyTo: ActorRef[JoinAck]) extends Command
  final case class LeaveRoom(roomId: String, user: String) extends Command
  final case class IncomingMessage(roomId: String, user: String, text: String) extends Command
  final case class GetStats(replyTo: ActorRef[Stats]) extends Command

  final case class JoinAck(success: Boolean, info: String)
  final case class Stats(activeRooms: Int, totalMessages: Long)

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    var rooms        = Map.empty[String, Set[String]]
    var totalMessages = 0L

    Behaviors.receiveMessage {
      case JoinRoom(roomId, user, replyTo) =>
        val users = rooms.getOrElse(roomId, Set.empty) + user
        rooms = rooms.updated(roomId, users)
        ChatMetrics.setActiveRooms(rooms.size.toLong)
        replyTo ! JoinAck(success = true, s"Joined room $roomId as $user")
        Behaviors.same

      case LeaveRoom(roomId, user) =>
        rooms.get(roomId).foreach { users =>
          val updated = users - user
          rooms =
            if (updated.nonEmpty) rooms.updated(roomId, updated)
            else rooms - roomId
        }
        ChatMetrics.setActiveRooms(rooms.size.toLong)
        Behaviors.same

      case IncomingMessage(roomId, user, text) =>
        totalMessages += 1
        ChatMetrics.incMessagesTotal()
        context.log.info(s"[$roomId] <$user>: $text")
        Behaviors.same

      case GetStats(replyTo) =>
        replyTo ! Stats(activeRooms = rooms.size, totalMessages = totalMessages)
        Behaviors.same
    }
  }
}
