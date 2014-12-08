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

  private val apiKeyParam = "&api_key=aa4ffe4729c78863475a1c3b082308fe"
  private val configurationUrl = "https://api.themoviedb.org/3/configuration?"
  private val imageUrl = "https://api.themoviedb.org/3/movie/imdbId/images?"

  private val largeSize = "w342"
  private val thumbnailSize = "w92"

  var imageBaseUrl = ""

  def main(args: Array[String]) {
    val configJson = getBaseConfigurationJson()
    val baseUrl = getImageBaseUrl(configJson, "base_url")
    println(baseUrl)
    val posterFileName = getMoviePosterFileName("tt1790864")
    val imageUrl: Option[String] = posterFileName match {
      case None => None
      case a => Option(baseUrl.get + thumbnailSize + a.get)
    }
    println(imageUrl)
  }

  def init() {
    val configJson = getBaseConfigurationJson()
    imageBaseUrl = getImageBaseUrl(configJson, "base_url").getOrElse("")
    println(s"Base URL for TheMovieDb is '$imageBaseUrl'")
  }

  def getThumbnailMoviePosterUrl(imdbId: String): Option[String] = {
    getMoviePosterUrlByWidth(imdbId, thumbnailSize)
  }

  def getBigMoviePosterUrl(imdbId: String): Option[String] = {
    getMoviePosterUrlByWidth(imdbId, largeSize)
  }

  def getActorData(name: String): JsValue = {
    val actorJson = getJsonFromRequest(s"https://api.themoviedb.org/3/search/person?query=${name.replace(" ", "+")}$apiKeyParam")
    val resultsArray = getJsArray(actorJson, "results")
    resultsArray match {
      case None => Json.obj()
      case a: Some[JsArray] => {
        val result = a.get(0)
        println(result)
        val movieDbId = getIntValue(result, "id").get
        val movieDbPosterName = getStringValue(result, "profile_path").get
        
        Json.obj("name" -> name,
          "id" -> movieDbId,
          "poster" -> (imageBaseUrl + largeSize + movieDbPosterName))
      }
    }
  }

  private def getJsArray(json: JsValue, arrayName: String): Option[JsArray] = {
    (json \ arrayName).validate[JsArray] match {
      case s: JsSuccess[JsArray] => Option(s.get)
      case e: JsError => None
    }
  }

  private def getStringValue(json: JsValue, tagName: String): Option[String] = {
    (json \ tagName).validate[String] match {
      case s: JsSuccess[String] => Option(s.get)
      case e: JsError => None
    }
  }

  private def getIntValue(json: JsValue, tagName: String): Option[Int] = {
    (json \ tagName).validate[Int] match {
      case s: JsSuccess[Int] => Option(s.get)
      case e: JsError => None
    }
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
    getJsonFromRequest(configurationUrl + apiKeyParam)
  }

  private def getMoviePosterFileName(imdbId: String): Option[String] = {
    val imageJsJson = getJsonFromRequest(imageUrl.replace("imdbId", imdbId) + apiKeyParam + "&external_source=imdb_id")
    println(imageJsJson)
    getImage(imageJsJson)
  }

  private def getJsonFromRequest(urlString: String): JsValue = {
    println(urlString)
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