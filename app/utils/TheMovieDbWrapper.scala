package utils

import javax.inject.Singleton

import dispatch._
import dispatch.Defaults._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.JsError
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess
import play.api.Logger

@Singleton
class TheMovieDbWrapper {
  
  private val logger = Logger("TheMovieDbWrapper")

  private val baseUrl = "https://api.themoviedb.org/3/"
  private val configurationPath = "configuration?"
  private val moviePosterPath = "movie/imdbId/images?"
  private val personPath = "person/"

  private val apiKeyParam = "api_key=aa4ffe4729c78863475a1c3b082308fe"

  private val largeSize = "w342"
  private val thumbnailSize = "w92"

  var imageBaseUrl = getImageBaseUrl(getBaseConfigurationJson(), "base_url").getOrElse("")

  def main(args: Array[String]) {
    val configJson = getBaseConfigurationJson()
    val baseUrl = getImageBaseUrl(configJson, "base_url")
    logger.info(s"Base URL: ${baseUrl.getOrElse("not found")}")
    val posterFileName = getMoviePosterFileName("tt1790864")
    val imageUrl: Option[String] = posterFileName match {
      case None => None
      case a => Option(baseUrl.get + thumbnailSize + a.get)
    }
    logger.info(s"Image URL: ${imageUrl.getOrElse("not found")}")
  }

//  def init() {
//    val configJson = getBaseConfigurationJson()
//    imageBaseUrl = getImageBaseUrl(configJson, "base_url").getOrElse("")
//    logger.info(s"Base URL for TheMovieDb is '$imageBaseUrl'")
//  }
//
  def getThumbnailMoviePosterUrl(imdbId: String): Option[String] = {
    getMoviePosterUrlByWidth(imdbId, thumbnailSize)
  }

  def getBigMoviePosterUrl(imdbId: String): Option[String] = {
    getMoviePosterUrlByWidth(imdbId, largeSize)
  }

  def getPersonData(name: String): Option[JsValue] = {
    logger.info(s"Trying to locate actor data for $name from Movie DB")
    val (movieDbId, movieDbPosterName) = getActorIdAndPoster(name)
    movieDbId match {
      case id: Some[Int] => {
        val (biography, birthday, birthplace, deathday, imdbId) = getActorInfo(movieDbId.get)
        logger.debug((biography, birthday, birthplace, deathday, imdbId).toString)

        val posterUrl = movieDbPosterName match {
          case poster: Some[String] => s"$imageBaseUrl$largeSize${movieDbPosterName.get}"
          case None => "/assets/images/no-image.jpg"
        }

        if (birthday.isEmpty && birthplace.isEmpty && deathday.isEmpty && biography.isEmpty && (imdbId.isEmpty || imdbId.get.isEmpty())) {
          None
        } else {
          Option(Json.obj("name" -> name,
            "movieDbId" -> movieDbId,
            "birthDay" -> birthday,
            "birthPlace" -> birthplace,
            "deathDay" -> deathday,
            "biography" -> biography,
            "imdbUrl" -> s"http://www.imdb.com/name/${imdbId.getOrElse("")}",
            "posterUrl" -> posterUrl))
        }
      }
      case None => None
    }
  }

  private def getActorIdAndPoster(name: String): (Option[Int], Option[String]) = {
    val actorJson = getJsonFromRequest(s"${baseUrl}search/person?query=${name.replace(" ", "+")}&$apiKeyParam")
    JsonUtil.getIntValue(actorJson, "total_results") match {
      case i: Some[Int] => {
        if (i.get >= 1) {
          val resultsArray = JsonUtil.getJsArray(actorJson, "results")
          resultsArray match {
            case None => (None, None)
            case a: Some[JsArray] => {
              val result = a.get(0)
              logger.debug(result.toString)
              val movieDbId = JsonUtil.getIntValue(result.get, "id")
              val movieDbPosterName = JsonUtil.getStringValue(result.get, "profile_path")
              (movieDbId, movieDbPosterName)
            }
          }
        } else {
          logger.info(s"No information found for actor: $name")
          (None, None)
        }
      }
      case None => {
        logger.info(s"Invalid JSON returned for actor search: $name")
        (None, None)
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
    logger.info(imageJsJson.toString)
    getImage(imageJsJson)
  }

  private def getJsonFromRequest(urlString: String): JsValue = {
    logger.debug(urlString)
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