package com.example.app.servlets

import com.example.app.auth.AuthenticationSupport
import com.example.app.db.Tables
import com.example.app.model.UserSpecialty
import org.json4s.JsonAST.JString
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{CorsSupport, ScalatraServlet}
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class SpecialtyServlet(val db: Database) extends ScalatraServlet with JacksonJsonSupport with AuthenticationSupport with CorsSupport {

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
    val future = db.run(Tables.specialties.result)
    val specialties = Await.result(future, 30.seconds)
    specialties
  }

  def checkIfDoctor(us: UserSpecialty): Boolean = {
    val future = db.run(Tables.findRoleByUserId(Some(us.userId)).result)
    val role = Await.result(future, 30.seconds)
    role.headOption match {
      case Some(r) =>
        r == "Doctor"
      case None => false
    }
  }

  post("/") {
    val requestBody = request.body
    val jsonBody = parse(requestBody)
    val specialtyOpt = (jsonBody \ "specialtyId").toOption
    val userIdOpt = (jsonBody \ "userId").toOption

    val userSpecialty = {
      for {
        specialtyId <- specialtyOpt
        userId <- userIdOpt
      } yield UserSpecialty(None, userId.extract[Long], specialtyId.extract[Long])
    }

    userSpecialty match {
      case Some(us) =>
        val isDoctor = checkIfDoctor(us)
        if (isDoctor) {
          val future = db.run(Tables.usersSpecialties += us)
          Await.result(future, 30.seconds)
          logger.info(s"$us association was persisted")
          JString("ok")
        } else {
          JString("You are trying to give specialty to a patient!")
        }
      case None =>
        JString("Not Persisted")
    }

  }

}
