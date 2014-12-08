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

  private val apiKeyParam = "api_key=aa4ffe4729c78863475a1c3b082308fe"
  private val configurationUrl = "https://api.themoviedb.org/3/configuration?"
  private val imageUrl = "https://api.themoviedb.org/3/movie/imdbId/images?"

  var imageBaseUrl = ""

  def main(args: Array[String]) {
    val configJson = getBaseConfigurationJson()
    val baseUrl = getImageBaseUrl(configJson, "base_url")
    println(baseUrl)
    val posterFileName = getMoviePosterFileName("tt1790864")
    val imageUrl: Option[String] = posterFileName match {
      case None => None
      case a => Option(baseUrl.get + "w92" + a.get)
    }
    println(imageUrl)
  }

  def init() {
    val configJson = getBaseConfigurationJson()
    imageBaseUrl = getImageBaseUrl(configJson, "base_url").getOrElse("")
    println(s"Base URL for TheMovieDb is '$imageBaseUrl'")
  }

  def getThumbnailMoviePosterUrl(imdbId: String): Option[String] = {
    getMoviePosterUrlByWidth(imdbId, "w92")
  }

  def getBigMoviePosterUrl(imdbId: String): Option[String] = {
    getMoviePosterUrlByWidth(imdbId, "w342")
  }

  private def getMoviePosterUrlByWidth(imdbId: String, width: String): Option[String] = {
    val posterFileName = getMoviePosterFileName(imdbId)
    val imageUrl: Option[String] = posterFileName match {
      case None => None
      case a => Option(imageBaseUrl + width + a.get)
    }
    imageUrl
  }

  private def getBaseConfigurationJson(): JsValue = {
    getJsonFromReqest(configurationUrl + apiKeyParam)
  }

  private def getMoviePosterFileName(imdbId: String): Option[String] = {
    val imageJsJson = getJsonFromReqest(imageUrl.replace("imdbId", imdbId) + apiKeyParam + "&external_source=imdb_id")
    println(imageJsJson)
    getImage(imageJsJson)
  }
  
  private def getJsonFromReqest(urlString: String):JsValue = {
    val request = url(urlString)
    val response = Http(request OK as.String)

    val jsonString = Await.result(response, Duration(10, "s"))
    Json.parse(jsonString)
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

  private def getImageBaseUrl(jsValue: JsValue, tag: String): Option[String] = {
    (jsValue \ "images" \ tag).validate[String] match {
      case s: JsSuccess[String] => Option(s.get)
      case e: JsError => None
    }
  }
}