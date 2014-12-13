package utils

import model.Movie2
import play.api.libs.json.JsValue
import dispatch._
import dispatch.Defaults._
import scala.concurrent.Await
import play.api.libs.json.Json
import java.net.URLEncoder
import scala.concurrent.duration.Duration

object OmdbWrapper {
  
  def fromOmdb(omdbDataJsValue: JsValue, folder: Int, dvd: Int): Movie2 = {
    val title = JsonUtil.getStringValue(omdbDataJsValue, "Title")
    val year = JsonUtil.getStringValue(omdbDataJsValue, "Year")
    val genres = JsonUtil.getStringValue(omdbDataJsValue, "Genre").get.split(", ")
    genres.foreach(_.trim())
    val languageRaw = JsonUtil.getStringValue(omdbDataJsValue, "Language")
    val language = languageRaw.get.split(",")(0).trim()
    val imdbId = JsonUtil.getStringValue(omdbDataJsValue, "imdbID").get
    val plot = JsonUtil.getStringValue(omdbDataJsValue, "Plot")
    val actors = JsonUtil.getStringValue(omdbDataJsValue, "Actors")
    val writer = JsonUtil.getStringValue(omdbDataJsValue, "Writer")
    val director = JsonUtil.getStringValue(omdbDataJsValue, "Director")
    val runtime = JsonUtil.getStringValue(omdbDataJsValue, "Runtime")
    val rating = JsonUtil.getStringValue(omdbDataJsValue, "imdbRating")
    val ratingResolved: Option[Double] = rating match {
      case Some(_) => {
        rating.get match {
          case "N/A" => None
          case _ => Option(rating.get.toDouble)
        }
      }
      case None => None
    }

    val rated = JsonUtil.getStringValue(omdbDataJsValue, "Rated")

    val posterUrlOmdb = JsonUtil.getStringValue(omdbDataJsValue, "Poster")
    val imgUrl = posterUrlOmdb.getOrElse("")

    new Movie2(None, title.get, None, None, Option(language), None, Option(genres), Option(s"http://www.imdb.com/title/$imdbId"),
      year.get.toInt, folder, dvd, Option(imdbId), plot, actors, writer, director, runtime, ratingResolved, rated, Option(imgUrl))
  }
  
    private def getOmdbJsonByTitle(title: String): JsValue = {
    val request = url("http://www.omdbapi.com/?plot=full&t=" + URLEncoder.encode(title, "UTF-8"))
    val response = Http(request OK as.String)

    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
    Json.parse(omdbJsonString)
  }

  def getOmdbJsonById(id: String): JsValue = {
    val request = url("http://www.omdbapi.com/?plot=full&i=" + id)
    val response = Http(request OK as.String)

    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
    Json.parse(omdbJsonString)
  }

}