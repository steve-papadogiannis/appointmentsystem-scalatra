package com.example.app.servlets

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source
import org.scalatra.ScalatraServlet
import slick.jdbc.H2Profile.api._

class SSEServlet(val db: Database, eventsSource: Source[ServerSentEvent, NotUsed]) extends ScalatraServlet {

  get("/") {
    eventsSource
  }

}
