package com.example.app.servlets

import com.example.app.auth.AuthenticationSupport
import com.example.app.db.Tables
import com.example.app.model.UserRole
import org.json4s.JsonAST.JString
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{CorsSupport, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport
import scala.concurrent.Await
import slick.jdbc.H2Profile.api._
import scala.concurrent.duration._

class RoleServlet(val db: Database) extends ScalatraServlet with JacksonJsonSupport with AuthenticationSupport with CorsSupport {

  before("/") {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
  }

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  get("/") {
    val future = db.run(Tables.roles.result)
    val roles = Await.result(future, 30.seconds)
    logger.info("Fetching roles")
    roles
  }

  post("/") {
    val requestBody = request.body
    val jsonBody = parse(requestBody)
    val roleIdOpt = (jsonBody \ "roleId").toOption
    val userIdOpt = (jsonBody \ "userId").toOption

    val userRole = {
      for {
        roleId <- roleIdOpt
        userId <- userIdOpt
      } yield UserRole(None, userId.extract[Long], roleId.extract[Long])
    }

    userRole match {
      case Some(ur) =>
        val future = db.run(Tables.usersRoles += ur)
        Await.result(future, 30.seconds)
        logger.info(s"$ur association was persisted")
        JString("ok")
      case None =>
        JString("Not Persisted")
    }

  }

}
