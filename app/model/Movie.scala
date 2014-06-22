package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Movie(
  val id: Option[String] = None,
  val title: String,
  val alternativeTitle: Option[String] = None,
  val originalTitle: Option[String] = None,
  val language: Option[String] = None,
  val subTitle: Option[String] = None,
  val genres: Option[Array[String]] = None,
  val url: Option[String] = None,
  val year: Int,
  val folder: Int,
  val dvd: Int)

object Movie {

  implicit val movieReads: Reads[Movie] = (
    (__ \ "id").readNullable[String] and
    (__ \ "title").read[String] and
    (__ \ "alternativeTitle").readNullable[String] and
    (__ \ "originalTitle").readNullable[String] and
    (__ \ "language").readNullable[String] and
    (__ \ "subTitle").readNullable[String] and
    (__ \ "genre").readNullable[Array[String]] and
    (__ \ "url").readNullable[String] and
    (__ \ "year").read[Int](min(0) keepAnd max(2030)) and //TODO reset min year
    (__ \ "folder").read[Int](min(1) keepAnd max(10)) and
    (__ \ "dvd").read[Int](min(1) keepAnd max(300)))(Movie.apply _)

  implicit val movieJsonWrites = new Writes[Movie] {
    def writes(movie: Movie) = Json.obj(
      "id" -> movie.id,
      "title" -> movie.title,
      "originalTitle" -> movie.originalTitle,
      "alternativeTitle" -> movie.alternativeTitle,
      "subTitle" -> movie.subTitle,
      "genre" -> movie.genres,
      "language" -> movie.language,
      "year" -> movie.year,
      "url" -> movie.url,
      "folder" -> movie.folder,
      "dvd" -> movie.dvd)
  }

  def toJson(movie: Movie): JsValue = {
    Json.toJson(movie)
  }
}