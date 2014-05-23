package model

import play.api.libs.json._

case class Movie(val title: String,
  val alternativeTitle: String,
  val originalTitle: String,
  val language: String,
  val subTitle: String,
  val genres: String,
  val url: String,
  val releaseYear: Int)

object Movie {

  implicit val movieJsonWrites = new Writes[Movie] {
    def writes(movie: Movie) = Json.obj(
      "title" -> movie.title,
      "genre" -> movie.genres,
      "year" -> movie.releaseYear)
  }

  def toJSON(movie: Movie): JsValue = {
    Json.toJson(movie)
  }
}