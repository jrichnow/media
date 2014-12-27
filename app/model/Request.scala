package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Request(
  val id: Option[String] = None,
  val subject: String,
  val topic: String,
  val comment: Option[String],
  val imdbUrl: Option[String])

object Request {

  implicit val requestReads: Reads[Request] = (
    (__ \ "id").readNullable[String] and
    (__ \ "subject").read[String] and
    (__ \ "topic").read[String] and
    (__ \ "comment").readNullable[String] and
    (__ \ "imdbUrl").readNullable[String])(Request.apply _)

  implicit val requestJsonWrites = new Writes[Request] {
    def writes(request: Request) = Json.obj(
      "id" -> request.id,
      "subject" -> request.subject,
      "topic" -> request.topic,
      "comment" -> request.comment,
      "imdbUrl" -> request.imdbUrl)
  }

  def toJson(request: Request): JsValue = {
    Json.toJson(request)
  }
}