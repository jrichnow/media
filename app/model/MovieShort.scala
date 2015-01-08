package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class MovieShort (
  val imdbId: String,
  val folder: Option[Int],
  val dvd: Option[Int],
  val hd: Option[Int])
  
object MovieShort {
  
  implicit val imdbMovieReads: Reads[MovieShort] = (
    (__ \ "imdbId").read[String] and
    (__ \ "folder").readNullable[Int] and
    (__ \ "dvd").readNullable[Int] and
    (__ \ "hd").readNullable[Int] )(MovieShort.apply _)
}