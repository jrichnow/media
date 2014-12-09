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

  private val baseUrl = "https://api.themoviedb.org/3/"
  private val configurationPath = "configuration?"
  private val moviePosterPath = "movie/imdbId/images?"
  private val personPath = "person/"

  private val apiKeyParam = "api_key=aa4ffe4729c78863475a1c3b082308fe"

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
    val (movieDbId, movieDbPosterName) = getActorIdAndPoster(name)
    val (biography, birthday, birthplace, deathday, imdbId) = getActorInfo(movieDbId.get)
    
    Json.obj("name" -> name,
      "id" -> movieDbId,
      "birthday" -> birthday,
      "birthplacce" -> birthplace,
      "deathday" -> deathday,
      "biography" -> biography,
      "imdbUrl" -> s"http://www.imdb.com/name/${imdbId.get}",
      "poster" -> (s"$imageBaseUrl$largeSize${movieDbPosterName.get}"))
  }

  private def getActorIdAndPoster(name: String): (Option[Int], Option[String]) = {
    val actorJson = getJsonFromRequest(s"${baseUrl}search/person?query=${name.replace(" ", "+")}&$apiKeyParam")
    val resultsArray = JsonUtil.getJsArray(actorJson, "results")
    resultsArray match {
      case None => (None, None)
      case a: Some[JsArray] => {
        val result = a.get(0)
        println(result)
        val movieDbId = JsonUtil.getIntValue(result, "id")
        val movieDbPosterName = JsonUtil.getStringValue(result, "profile_path")
        (movieDbId, movieDbPosterName)
      }
    }
  }

  private def getActorInfo(movieDbId: Int): (Option[String], Option[String], Option[String], Option[String], Option[String]) = {
    val actorJson = getJsonFromRequest(s"${baseUrl}person/${movieDbId}?$apiKeyParam")
    
    val biography = JsonUtil.getStringValue(actorJson, "biography")
    val birthday = JsonUtil.getStringValue(actorJson, "birthday")
    val birthplace = JsonUtil.getStringValue(actorJson, "place_of_birth")
    val deathday = JsonUtil.getStringValue(actorJson, "deathday")
    val imdbId = JsonUtil.getStringValue(actorJson, "imdb_id")
    
    (biography, birthday, birthplace, deathday, imdbId)
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
    getJsonFromRequest(s"${baseUrl}${configurationPath}${apiKeyParam}")
  }

  private def getMoviePosterFileName(imdbId: String): Option[String] = {
    val imageJsJson = getJsonFromRequest(s"${baseUrl}${moviePosterPath.replace("imdbId", imdbId)}${apiKeyParam}&external_source=imdb_id")
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