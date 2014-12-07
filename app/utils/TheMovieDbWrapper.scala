package utils

import dispatch._
import dispatch.Defaults._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess

object TheMovieDbWrapper {

  private val apiKey = "aa4ffe4729c78863475a1c3b082308fe"
  private val configurationUrl = "https://api.themoviedb.org/3/configuration?api_key="
  private val imageUrl = "https://api.themoviedb.org/3/movie/imdbId/images?api_key="

  var baseUrl = ""

  def main(args: Array[String]) {
    val configJson = getBaseConfiguration
    val baseUrl = getBaseUrl(configJson, "base_url")
    println(baseUrl)
    val posterFileName = getPosterFileName("tt1790864")
    val imageUrl: Option[String] = posterFileName match {
      case None => None
      case a => Option(baseUrl.get + "w92" + a.get)
    }
    println(imageUrl)
  }

  def init() {
    val configJson = getBaseConfiguration
    baseUrl = getBaseUrl(configJson, "base_url").getOrElse("")
    println(s"Base URL for TheMovieDb is '$baseUrl'")
  }

  def getThumbnailPosterUrl(imdbId: String): Option[String] = {
    getPosterUrlByWidth(imdbId, "w92")
  }

  def getPosterUrl(imdbId: String): Option[String] = {
    getPosterUrlByWidth(imdbId, "w342")
  }

  private def getPosterUrlByWidth(imdbId: String, width: String): Option[String] = {
    val posterFileName = getPosterFileName(imdbId)
    val imageUrl: Option[String] = posterFileName match {
      case None => None
      case a => Option(baseUrl + width + a.get)
    }
    imageUrl
  }

  private def getBaseConfiguration(): JsValue = {
    val request = url(configurationUrl + apiKey)
    val response = Http(request OK as.String)

    val configJsonString = Await.result(response, Duration(10, "s"))
    Json.parse(configJsonString)
  }

  private def getPosterFileName(imdbId: String): Option[String] = {
    val request = url(imageUrl.replace("imdbId", imdbId) + apiKey + "&external_source=imdb_id")
    val response = Http(request OK as.String)

    val imageJsonString = Await.result(response, Duration(10, "s"))
    val imageJsJson = Json.parse(imageJsonString)
    println(imageJsJson)
    getImage(imageJsJson)
  }

  private def getImage(imageJsValue: JsValue): Option[String] = {
    val imageArray = (imageJsValue \ "posters").validate[JsArray] match {
      case s: JsSuccess[JsArray] => Option(s.get)
      case e: JsError => None
    }

    imageArray match {
      case None => None
      case _ => {
        (imageArray.get(0) \ "file_path").validate[String] match {
          case s: JsSuccess[String] => Option(s.get)
          case e: JsError => None
        }
        // Just take the first one for the time being
        //        if (imageArray.size == 1) {
        //          
        //        }
        //        else {
        // Find the english version
        //        	imageArray.foreach(f)
        //        }
      }
    }
  }

  private def getBaseUrl(jsValue: JsValue, tag: String): Option[String] = {
    (jsValue \ "images" \ tag).validate[String] match {
      case s: JsSuccess[String] => Option(s.get)
      case e: JsError => None
    }
  }
}