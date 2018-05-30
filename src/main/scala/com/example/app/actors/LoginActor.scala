package com.example.app.actors

import akka.actor.{Actor, ActorRef}
import com.example.app.db.Tables
import com.example.app.model.{Outcome, User}
import slick.jdbc.H2Profile.api._
import org.json4s.DefaultFormats
import org.json4s.Formats
import org.json4s.JsonDSL._
import org.json4s._
import com.example.app.model.UserSerializer._

import scala.concurrent.ExecutionContext

class LoginActor(val db: Database, val sseActor: ActorRef) extends Actor {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  protected implicit def executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def receive: PartialFunction[Any, Unit] = {
    case Some(user: User) =>
      db.run(Tables.findRoleByUserId(user.id).result.headOption).map {
        case Some(role) => sseActor ! Outcome("OK", "Returning user info", JObject(user) ~ ("role" -> role))
        case None => sseActor ! Outcome("OK", "Returning user but no role found", JObject(user))
      }
    case None => sseActor ! Outcome("Error", "No user found", JNothing);
  }
}
