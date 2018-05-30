package com.example.app.actors

import akka.actor.Actor
import com.example.app.model.Outcome
import org.json4s.JValue
import org.json4s.JsonAST.JValue

class SSEActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case Outcome(_, _, _) =>
      import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

  }
}
