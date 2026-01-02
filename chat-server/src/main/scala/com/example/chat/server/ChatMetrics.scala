package com.example.chat.server

import java.util.concurrent.atomic.AtomicLong

object ChatMetrics {
  private val messagesTotal    = new AtomicLong(0L)
  private val activeRoomsCount = new AtomicLong(0L)

  def incMessagesTotal(): Unit =
    messagesTotal.incrementAndGet()

  def setActiveRooms(count: Long): Unit =
    activeRoomsCount.set(count)

  def toPrometheusText(instanceId: String): String = {
    val m  = messagesTotal.get()
    val ar = activeRoomsCount.get()

    s"""
       |# HELP chat_messages_total Total number of chat messages processed by this server instance.
       |# TYPE chat_messages_total counter
       |chat_messages_total{instance="$instanceId"} $m
       |
       |# HELP chat_active_rooms Number of active chat rooms on this server instance.
       |# TYPE chat_active_rooms gauge
       |chat_active_rooms{instance="$instanceId"} $ar
       |""".stripMargin
  }
}
