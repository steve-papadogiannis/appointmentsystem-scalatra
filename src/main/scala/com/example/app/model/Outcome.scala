package com.example.app.model

import org.json4s.JValue

case class Outcome[T <: JValue](status: String, info: String, payload: T) {

}
