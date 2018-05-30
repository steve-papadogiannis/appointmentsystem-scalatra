package com.example.app.auth.strategies

import com.example.app.auth.TokenGenerator
import com.example.app.db.Tables
import com.example.app.model.User
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.{CookieOptions, ScalatraBase}
import org.scalatra.auth.ScentryStrategy
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.duration._
import scala.concurrent.Await
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._

class RememberMeStrategy(protected val app: ScalatraBase, protected val db: Database)(implicit request: HttpServletRequest,
                                                                                      response: HttpServletResponse)
  extends ScentryStrategy[User] {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def name: String = "RememberMe"

  val COOKIE_KEY = "rememberMe"

  private val oneWeek = 7 * 24 * 3600

  /**
    * Grab the value of the rememberMe cookie token.
    */
  private def tokenVal = {
    app.cookies.get(COOKIE_KEY) match {
      case Some(token) => token
      case None => ""
    }
  }

  /**
    * Determine whether the strategy should be run for the current request.
    * @param request The HttpServletRequest under check
    * @return True if the request is valid for this authentication strategy.
    *         False otherwise.
    */
  override def isValid(implicit request: HttpServletRequest): Boolean = {
    val isValid = tokenVal != ""
    logger.info(s"Is valid to authenticate with RememberMeStrategy: $isValid")
    isValid
  }

  /**
    * Get a user from a given token
    * @param request The HttpServletRequest which we want to authenticate the user for.
    * @param response The HttpServletResponse which will be returned to the client.
    * @return The user if the tokenVal corresponds to a user which is already authenticated.
    *         None otherwise.
    */
  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    logger.info("Attempting authentication with RememberMeStrategy")
    val future = db.run(Tables.findUserByToken(tokenVal).result.headOption)
    Await.result(future, 30.seconds).filter(user => user.token match {
      case Some(token) => token == tokenVal
      case None        => false
    }).map(user => {
      logger.info(s"Successful Login for $user")
      user
    }).orElse({
      logger.error(s"Unsuccessful Login for token $tokenVal")
      None
    })
  }

  /**
    * What should happen if the user is currently not authenticated?
    */
  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("Unauthenticated user")
    // We inform the client with a standard http status code. (Unauthorized)
    app.halt(401)
  }

  /**
    * After successfully authenticating with either the RememberMeStrategy, or the UserPasswordStrategy with the
    * "remember me" checkbox checked, we set a rememberMe cookie for later use.
    *
    * NB make sure you set a cookie path, or you risk getting weird problems because you've accidentally set
    * more than 1 cookie.
    *
    * @param request The HttpServletRequest which we want to authenticate the user for.
    * @param response The HttpServletResponse which will be returned to the client.
    * @return Unit
    */
  override def afterAuthenticate(winningStrategy: String, user: User)(implicit request: HttpServletRequest,
                                                                      response: HttpServletResponse): Unit = {
    if (winningStrategy == "RememberMe" ||
        (winningStrategy == "UserPassword" && checkbox2boolean(app.params.get("rememberMe").getOrElse("").toString))) {

      val token = TokenGenerator.generateSHAToken(user.username)

      val future = db.run(Tables.findUserByIdProjectToken(user.id).update(Some(token)))
      Await.result(future, 30.seconds)
      logger.info(s"New token for ${user.id} is $token")
      app.cookies.set(COOKIE_KEY, token)(CookieOptions(maxAge = oneWeek, path = "/"))
    }
  }

  /**
    * Run this code before logout, to clean up any leftover database state and delete the rememberMe token cookie.
    */
  override def beforeLogout(user: User)(implicit request: HttpServletRequest, response: HttpServletResponse): Unit = {
    logger.info(s"Forgeting token for user ${user.id}")
    if (user != null){
      user.forgetMe(db)
    }
    app.cookies.delete(COOKIE_KEY)(CookieOptions(path = "/"))
  }

  /**
    * Used to easily match a checkbox value
    */
  private def checkbox2boolean(s: String): Boolean = {
    s match {
      case "true" => true
      case _ => false
    }
  }
}