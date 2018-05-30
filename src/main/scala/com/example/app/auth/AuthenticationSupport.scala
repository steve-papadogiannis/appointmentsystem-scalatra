package com.example.app.auth

import com.example.app.auth.strategies.{RememberMeStrategy, UserPasswordStrategy}
import com.example.app.db.Tables
import com.example.app.model.User
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.ScalatraBase
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Await
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._
import scala.concurrent.duration._

trait AuthenticationSupport extends ScalatraBase with ScentrySupport[User] {
  self: ScalatraBase =>

  def db: Database

  protected def fromSession: PartialFunction[String, User] = {
    case id: String =>
      val future = db.run(Tables.findUserById(id.toLong).result.headOption)
      Await.result(future, 30.seconds).map(user => {
        logger.info(s"Successful mapping of session from $id to $user")
        user
      }).orElse({
        logger.error(s"Unsuccessful mapping of session from $id")
        None
      }).get
  }

  protected def toSession: PartialFunction[User, String] = {
    case usr: User =>
      usr.id.map(_.toString).getOrElse("")
  }

  protected val scentryConfig: ScentryConfiguration = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]

  val logger: Logger = LoggerFactory.getLogger(getClass)

  protected def requireLogin(): Unit = {
    if (!isAuthenticated) {
      halt(401)
    }
  }

  /**
    * If an unauthenticated user attempts to access a route which is protected by Scentry,
    * run the unauthenticated() method on the UserPasswordStrategy.
    */
  override protected def configureScentry: Unit = {
    scentry.unauthenticated {
      scentry.strategies("UserPassword").unauthenticated()
    }
  }

  /**
    * Register auth strategies with Scentry. Any controller with this trait mixed in will attempt to
    * progressively use all registered strategies to log the user in, falling back if necessary.
    */
  override protected def registerAuthStrategies: Unit = {
    scentry.register("RememberMe", app => new RememberMeStrategy(app, db))
    scentry.register("UserPassword", app => new UserPasswordStrategy(app, db))
  }

}