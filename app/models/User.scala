package models

import play.api.libs.json._

case class User(id: Long, username: String, password: String)

object User {  
  implicit val userFormat = Json.format[User]
}
