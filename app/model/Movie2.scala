package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Movie2(
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
  val dvd: Int,
  val imdbId: Option[String],
  val plot: Option[String],
  val actors: Option[String],
  val writer: Option[String],
  val director: Option[String],
  val runtime: Option[String],
  val rating: Option[Double],
  val rated: Option[String],
  val imageUrl: Option[String])

object Movie2 {

  implicit val movieReads: Reads[Movie2] = (
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
    (__ \ "dvd").read[Int](min(1) keepAnd max(300)) and
    (__ \ "imdbId").readNullable[String] and
    (__ \ "plot").readNullable[String] and
    (__ \ "actors").readNullable[String] and
    (__ \ "writer").readNullable[String] and
    (__ \ "director").readNullable[String] and
    (__ \ "runtime").readNullable[String] and
    (__ \ "rating").readNullable[Double] and
    (__ \ "rated").readNullable[String] and
    (__ \ "imageUrl").readNullable[String])(Movie2.apply _)

  implicit val movieJsonWrites = new Writes[Movie2] {
    def writes(movie: Movie2) = Json.obj(
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
      "dvd" -> movie.dvd,
      "imdbId" -> movie.imdbId,
      "plot" -> movie.plot,
      "actors" -> movie.actors,
      "writer" -> movie.writer,
      "director" -> movie.director,
      "runtime" -> movie.runtime,
      "rating" -> movie.rating,
      "rated" -> movie.rated,
      "imageUrl" -> movie.imageUrl)
  }

  def fromOmdb(omdbDataJsValue: JsValue, folder: Int, dvd: Int): Movie2 = {
    val title = getValue(omdbDataJsValue, "Title")
    val year = getValue(omdbDataJsValue, "Year")
    val genres = getValue(omdbDataJsValue, "Genre").get.split(",")
    val languageRaw = getValue(omdbDataJsValue, "Language")
    val language = languageRaw.get.split(",")(0).trim()
    val imdbId = getValue(omdbDataJsValue, "imdbID").get
    val plot = getValue(omdbDataJsValue, "Plot")
    val actors = getValue(omdbDataJsValue, "Actors")
    val writer = getValue(omdbDataJsValue, "Writer")
    val director = getValue(omdbDataJsValue, "Director")
    val runtime = getValue(omdbDataJsValue, "Runtime")
    val rating = getValue(omdbDataJsValue, "imdbRating")
    val ratingResolved: Option[Double] = rating match {
      case Some(_) => {
        rating.get match {
          case "N/A" => None
          case _ => Option(rating.get.toDouble)
        }
      }
      case None => None
    }

    val rated = getValue(omdbDataJsValue, "Rated")

    val posterUrlOmdb = getValue(omdbDataJsValue, "Poster")
    val imgUrl = posterUrlOmdb.getOrElse("")

    new Movie2(None, title.get, None, None, Option(language), None, Option(genres), Option(s"http://www/imdb.com/$imdbId"),
      year.get.toInt, folder, dvd, Option(imdbId), plot, actors, writer, director, runtime, ratingResolved, rated, Option(imgUrl))
  }

  def getValue(omdbDataJsValue: JsValue, tag: String): Option[String] = {
    (omdbDataJsValue \ tag).validate[String] match {
      case s: JsSuccess[String] => Option(s.get)
      case e: JsError => None
    }
  }

  def toJson(movie: Movie2): JsValue = {
    Json.toJson(movie)
  }
}