package model

import play.api.libs.json._

case class Movie(val title: String,
  val alternativeTitle: String,
  val originalTitle: String,
  val language: String,
  val subTitle: String,
  val genres: String,
  val url: String,
  val releaseYear: Int,
  val folder: Int,
  val dvdNumber: Int)

object Movie {

  implicit val movieJsonWrites = new Writes[Movie] {
    def writes(movie: Movie) = Json.obj(
      "title" -> movie.title,
      "originalTitle" -> movie.originalTitle,
      "alternativeTitle" -> movie.alternativeTitle,
      "subTitle" -> movie.subTitle,
      "genre" -> movie.genres,
      "language" -> movie.language,
      "year" -> movie.releaseYear,
      "url" -> movie.url,
      "folder" -> movie.folder,
      "dvd" -> movie.dvdNumber)
  }

  def toJSON(movie: Movie): JsValue = {
    Json.toJson(movie)
  }
}