package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import utils.TheMovieDbWrapper

case class Movie(
  id: Option[String] = None,
  title: String,
  alternativeTitle: Option[String] = None,
  originalTitle: Option[String] = None,
  language: Option[String] = None,
  subTitle: Option[String] = None,
  genres: Option[Array[String]] = None,
  url: Option[String] = None,
  year: Int,
  folder: Option[Int],
  dvd: Option[Int],
  hd: Option[Int] = None,
  imdbId: Option[String] = None,
  plot: Option[String] = None,
  actors: Option[String] = None,
  writer: Option[String] = None,
  director: Option[String] = None,
  runtime: Option[String] = None,
  rating: Option[Double] = None,
  rated: Option[String] = None,
  imageUrl: Option[String] = None,
  seen: Option[Boolean] = Some(false))

object Movie {

  val actorField = "actors" // TODO enums?
  val directorField = "director"
  val writerField = "writer"

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
    (__ \ "folder").readNullable[Int] and
    (__ \ "dvd").readNullable[Int] and
    (__ \ "hd").readNullable[Int] and
    (__ \ "imdbId").readNullable[String] and
    (__ \ "plot").readNullable[String] and
    (__ \ "actors").readNullable[String] and
    (__ \ "writer").readNullable[String] and
    (__ \ "director").readNullable[String] and
    (__ \ "runtime").readNullable[String] and
    (__ \ "rating").readNullable[Double] and
    (__ \ "rated").readNullable[String] and
    (__ \ "imageUrl").readNullable[String] and
    (__ \ "seen").readNullable[Boolean])(Movie.apply _)

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
      "dvd" -> movie.dvd,
      "hd" -> movie.hd,
      "imdbId" -> movie.imdbId,
      "plot" -> movie.plot,
      "actors" -> movie.actors,
      //      "actors" -> wrapActorsUrl(movie.actors),
      "writer" -> movie.writer,
      "director" -> movie.director,
      "runtime" -> movie.runtime,
      "rating" -> movie.rating,
      "rated" -> movie.rated,
      "imageUrl" -> validateImageUrl(movie.imageUrl.getOrElse("")),
      "seen" -> movie.seen)
    //      "imageUrl" -> validateImageUrl(getTheMovieDbImageUrl(movie.imdbId.get).getOrElse("")))
  }

  def getTheMovieDbImageUrl(imdbId: String): Option[String] = {
    new TheMovieDbWrapper().getBigMoviePosterUrl(imdbId)
  }

  private def validateImageUrl(imageUrl: String): String = {
    imageUrl match {
      case "N/A" => "/assets/images/no-image.jpg"
      case "" => "/assets/images/no-image.jpg"
      case _ => imageUrl
    }
  }

  private def wrapActorsUrl(actors: Option[String]): String = {
    actors match {
      case Some(_) => {
        val actorList = actors.get.split(", ").toList
        val actorUrlList = for (actor <- actorList) yield s"<a href='/movies/findUi?entity=Actor&name=$actor'>$actor</a>"
        actorUrlList.mkString(", ")
      }
      case None => ""
    }
  }

  def toJson(movie: Movie): JsValue = {
    Json.toJson(movie)
  }

  def toStringShort(movie: Movie): String = {
    s"${movie.title} (${movie.year})"
  }
}