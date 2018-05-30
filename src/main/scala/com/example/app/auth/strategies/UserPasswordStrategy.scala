package com.example.app.auth.strategies

import com.example.app.db.Tables
import com.example.app.model.User
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.json4s.JsonAST.{JField, JObject, JString}
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import org.slf4j.{Logger, LoggerFactory}
//import slick.jdbc.PostgresProfile.api._
import com.github.t3hnar.bcrypt._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.json4s._

class UserPasswordStrategy(protected val app: ScalatraBase, protected val db: Database)(implicit request: HttpServletRequest,
                                                            response: HttpServletResponse)
  extends ScentryStrategy[User] {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def name: String = "UserPassword"

  private val responseBody: String = {
    (app enrichRequest app.request) body
  }

  private val login: String = {
    val json = parse(responseBody)
    val usernames: List[String] = {
      for {
        JObject(child) <- json
        JField("username", JString(username)) <- child
      } yield username
    }
    usernames.headOption.getOrElse("")
  }

  private val password: String = {
    val json = parse(responseBody)
    val passwordOpt: List[String] = {
      for {
        JObject(child) <- json
        JField("password", JString(password)) <- child
      } yield password
    }
    passwordOpt.headOption.getOrElse("")
  }

  /**
    * Determine whether the strategy should be run for the current request.
    * @param request The HttpServletRequest under check
    * @return True if the request is valid for this authentication strategy.
    *         False otherwise.
    */
  override def isValid(implicit request: HttpServletRequest): Boolean = {
    val isValid = login != "" && password != ""
    logger.info(s"Is valid to authenticated with UserPasswordStrategy $isValid")
    isValid
  }

  /**
    * Get a user from a given token
    * @param request The HttpServletRequest which we want to authenticate the user for.
    * @param response The HttpServletResponse which will be returned to the client.
    * @return The user if the userName and password corresponds to a user.t
    *         None otherwise.
    */
  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    logger.info("Attempting authentication with UserPasswordStrategy")
    val future = db.run(Tables.findUserByUsername(login).result.headOption)
    Await.result(future, 30.seconds).filter(user => password.isBcrypted(user.password)).map(user => {
      logger.info(s"Successful Login for $user")
      user
    }).orElse({
      logger.error(s"Unsuccessful Login for username $login")
      None
    })
  }

  /**
    * What should happen if the user is currently not authenticated?
    */
  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("Unauthenticated user")
    app.halt(401)
  }

}

