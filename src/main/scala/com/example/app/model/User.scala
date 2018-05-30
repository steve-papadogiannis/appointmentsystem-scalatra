package com.example.app.model

import com.example.app.db.Tables
import org.json4s.{FieldSerializer, JField}
import org.json4s.FieldSerializer._
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._
import org.json4s.JsonDSL._
import scala.concurrent.duration._
import scala.concurrent.Await

case class User(id: Option[Long], username: String, password: String,
                firstName: String, lastName: String, email: String,
                token: Option[String], enabled: Boolean) {


  val logger: Logger = LoggerFactory.getLogger(getClass)

  def forgetMe(db: Database): Unit = {
    logger.info(s"Invalidating token for user with id $id")
    val future = db.run(Tables.findUserByIdProjectToken(id).update(None))
    Await.result(future, 30.seconds)
  }

}

object UserSerializer {

  implicit def user2ListJFields(user: User): List[JField] = {
    ("id" -> user.id) ~
    ("username" -> user.username) ~
    ("firstName" -> user.firstName) ~
    ("lastName" -> user.lastName) ~
    ("email" -> user.email) obj
  }

}


