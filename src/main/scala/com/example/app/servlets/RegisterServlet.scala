package com.example.app.servlets

import com.example.app.db.Tables
import com.example.app.model.{Outcome, User}
import org.json4s.DefaultFormats
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{CorsSupport, ScalatraServlet}
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
// JSON-related libraries
import org.json4s.Formats
// JSON handling support from Scalatra
import org.scalatra.json._
import com.github.t3hnar.bcrypt._
import scala.concurrent.duration._
// JSON handling support from Scalatra
import org.scalatra.json._
import org.json4s.JsonDSL._
import org.json4s._

class RegisterServlet(db: Database) extends ScalatraServlet with JacksonJsonSupport with CorsSupport {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  options("/*") {
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
  }

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  post("/") {

    logger.info("Registering new user")
    val requestBody = request.body
    val jsonBody = parse(requestBody)
    val usernameOpt = (jsonBody \ "username").toOption
    val passwordOpt = (jsonBody \ "password").toOption
    val firstNameOpt = (jsonBody \ "firstName").toOption
    val lastNameOpt = (jsonBody \ "lastName").toOption
    val emailOpt = (jsonBody \ "email").toOption

    val user = {
      for {
        username <- usernameOpt
        password <- passwordOpt
        firstName <- firstNameOpt
        lastName <- lastNameOpt
        email <- emailOpt
      } yield User(None, username.extract[String], password.extract[String].bcrypt,
        firstName.extract[String], lastName.extract[String], email.extract[String],
        None, enabled = true)
    }

    logger.info(s"User is $user")

    user match {
      case Some(u) =>
          val future = db.run(Tables.users += u)
          Await.result(future, 30.seconds)
          logger.info("User was persisted")
          val future2 = db.run(Tables.findUserByUsername(u.username).result.headOption)
          val user2 = Await.result(future2, 30.seconds)
          logger.info(s"User fetched from db is $user2")
          Outcome("OK", "User registered", ("id" -> u.id) ~
          ("username" -> u.username) ~
          ("firstName" -> u.firstName) ~
          ("lastName" -> u.lastName) ~
          ("email" -> u.email))
      case None => "status" -> "Not enough info"
    }

  }

}
