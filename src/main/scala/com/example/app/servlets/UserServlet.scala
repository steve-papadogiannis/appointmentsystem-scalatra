package com.example.app.servlets

import com.example.app.auth.AuthenticationSupport
import com.example.app.db.Tables
import org.scalatra.{FutureSupport, ScalatraServlet}
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class UserServlet(val db: Database) extends ScalatraServlet with FutureSupport with AuthenticationSupport {

  protected implicit def executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  /**
    * Users are required to be logged in before they can hit any of the routes in this controller.
    */
  before() {
    requireLogin()
  }

  get("/a") {
    "This is a protected controller action. If you can see it, you're logged in."
  }

  get("/") {
    // run the action and map the result to something more readable
    db.run(Tables.users.result) map { xs =>
      contentType = "text/plain"
      xs
    }
  }

  post("/") {

  }

}
