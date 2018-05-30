package com.example.app.servlets

import com.example.app.auth.AuthenticationSupport
import com.example.app.db.Tables
import org.scalatra.{CorsSupport, ScalatraServlet}

import scala.concurrent.Await
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._
// JSON-related libraries
import org.json4s.Formats
// JSON handling support from Scalatra
import org.scalatra.json._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import org.json4s.JsonDSL._
import org.json4s._
import com.example.app.model.UserSerializer._

class LoginServlet(val db: Database) extends ScalatraServlet with AuthenticationSupport with JacksonJsonSupport with CorsSupport {

  protected implicit def executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  before("/") {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
  }

  post("/") {
    val maybeUser = scentry.authenticate()
    val future = db.run(Tables.findRoleByUserId(maybeUser match {
      case Some(user) => user.id
      case None       => None
    }).result.headOption)
    val maybeRole: Option[String] = Await.result(future, 30.seconds)
    val json = maybeUser match {
      case Some(user) =>
          JObject(user) ~
          ("role" -> maybeRole)
      case None => JNothing
    }
    render(json)
  }

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

}