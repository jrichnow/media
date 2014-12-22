package model

import play.api.libs.json._
import play.api.libs.json.Reads
import play.api.libs.functional.syntax._

case class Actor(
  val id: Option[String] = None,
  val movieDbId: Option[Int] = None,
  val name: String,
  val birthDay: Option[String] = None,
  val birthPlace: Option[String] = None,
  val deathDay: Option[String] = None,
  val biography: Option[String] = None,
  val imdbUrl: Option[String] = None,
  val posterUrl: Option[String] = None)

object Actor {

  implicit val actorReads: Reads[Actor] = (
    (__ \ "id").readNullable[String] and
    (__ \ "movieDbId").readNullable[Int] and
    (__ \ "name").read[String] and
    (__ \ "birthDay").readNullable[String] and
    (__ \ "birthPlace").readNullable[String] and
    (__ \ "deathDate").readNullable[String] and
    (__ \ "biography").readNullable[String] and
    (__ \ "imdbUrl").readNullable[String] and
    (__ \ "posterUrl").readNullable[String])(Actor.apply _)

  implicit val actorJsonWrites = new Writes[Actor] {
    def writes(actor: Actor) = Json.obj(
      "id" -> actor.id,
      "movieDbId" -> actor.movieDbId,
      "name" -> actor.name,
      "birthDay" -> actor.birthDay,
      "birthPlace" -> actor.birthPlace,
      "deathDay" -> actor.deathDay,
      "biography" -> actor.biography,
      "imdbUrl" -> actor.imdbUrl,
      "posterUrl" -> actor.posterUrl)
  }

  def toJson(actor: Actor): JsValue = {
    Json.toJson(actor)
  }

  def fromJson(actorJson: JsValue): Option[Actor] = {
    Json.parse(actorJson.toString()).validate[Actor] match {
      case s: JsSuccess[Actor] => Option(s.get)
      case e: JsError => {
        println(e.toString)
        None
      }
    }
  }
}