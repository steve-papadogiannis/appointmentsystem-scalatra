package com.example.app.actors

import akka.actor.Actor
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import com.example.app.model.Outcome
import org.json4s.JValue
import org.json4s.JsonAST.JValue

class SSEActor(val source: SourceQueueWithComplete[String]) extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case message =>
      import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
      source.offer(message.toString)
  }
}
