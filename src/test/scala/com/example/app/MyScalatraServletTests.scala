package com.example.app

import com.example.app.servlets.UserServlet
import org.scalatra.test.scalatest._

class MyScalatraServletTests extends ScalatraFunSuite {

  addServlet(classOf[UserServlet], "/users/*")

  test("GET / on UserServlet should return status 401"){
    get("/"){
      status should equal (401)
    }
  }

}
