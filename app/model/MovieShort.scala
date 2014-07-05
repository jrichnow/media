package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class MovieShort (
  val imdbId: String,
  val folder: Int,
  val dvd: Int)
  
object MovieShort {
  
  implicit val imdbMovieReads: Reads[MovieShort] = (
    (__ \ "imdbId").read[String] and
    (__ \ "folder").read[Int](min(1) keepAnd max(10)) and
    (__ \ "dvd").read[Int](min(1) keepAnd max(300)))(MovieShort.apply _)
}