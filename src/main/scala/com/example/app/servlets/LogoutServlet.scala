package com.example.app.servlets

import com.example.app.auth.AuthenticationSupport
import org.scalatra.ScalatraServlet
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._

class LogoutServlet(val db: Database)  extends ScalatraServlet with AuthenticationSupport {

  post("/") {
    logger.info("Logging out...")
    scentry.logout()
  }

}
