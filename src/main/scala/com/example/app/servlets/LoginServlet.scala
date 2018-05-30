package com.example.app.servlets

import akka.actor.ActorRef
import com.example.app.auth.AuthenticationSupport
import com.example.app.model.Outcome
import org.scalatra.ScalatraServlet
import slick.jdbc.H2Profile.api._
import org.json4s.Formats
import org.scalatra.json._
import org.json4s._

class LoginServlet(val db: Database, loginActor: ActorRef) extends ScalatraServlet
  with AuthenticationSupport with JacksonJsonSupport {

  before("/") {
    contentType = formats("json")
  }

  post("/") {
    val maybeUser = scentry.authenticate()
    loginActor ! maybeUser
    Outcome("OK", "Login request submitted", JNothing)
  }

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats
}