package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import model.Movie
import scala.collection.immutable.Seq
import dispatch._
import dispatch.Defaults._
import java.net.URLEncoder
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Success
import scala.util.Failure
import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import dao.MovieDao

object Movies extends Controller {

  var movies: Seq[Movie] = Seq.empty

  def index = Action {
    Ok(views.html.movies.index())
  }

  def list = Action {
    Ok(Json.toJson(movies))
  }

  def title(title: String) = Action {
    Ok(views.html.movies.details(findByTitle(title)))
  }

  def add = Action(parse.json) { request =>
    val movieJsonString = request.body
    println(s"received add movie request: $movieJsonString")
    val movieJson = Json.toJson(movieJsonString)
    println(s"converted Json: $movieJson")

    val (isValid, jsonResult, movieOption) = validateJson(movieJson)
    if (isValid) {
      movies = movies :+ MovieDao.add(movieOption.get)
    }
    Ok(jsonResult)
  }

  def image(title: String) = Action {
    val omdbJson = getOmdbJson(title)
    var posterUrl: String = ""
    val posterUrlOmdb = (omdbJson \ "Poster").validate[String]
    posterUrlOmdb match {
      case s: JsSuccess[String] => {
        val omdb = posterUrlOmdb.get
        omdb match {
          case "N/A" => posterUrl = "/assets/images/no-image.jpg"
          case _ => posterUrl = omdb
        }
        Redirect(posterUrl)
      }
      case e: JsError => {
        println("Errors: " + JsError.toFlatJson(e).toString())
        Redirect("/assets/images/no-image.jpg")
      }
    }
  }

  def imdb(title: String) = Action {
    Ok(getOmdbJson(title))
  }

  def init(moviesSeq: Seq[Movie]): Unit = {
    movies = moviesSeq
  }

  def findByTitle(title: String): Movie = {
    movies.find(movie => movie.title == title).get
  }

  private def getOmdbJson(title: String): JsValue = {
    val request = url("http://www.omdbapi.com/?t=" + URLEncoder.encode(title, "UTF-8"))
    val response = Http(request OK as.String)

    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
    Json.parse(omdbJsonString)
  }
  
  private def validateJson(movieJson: JsValue): (Boolean, JsValue, Option[Movie]) = {
    movieJson.validate[Movie] match {
      case s: JsSuccess[Movie] => {
        (true, Json.obj("validation" -> true, "redirectPath" -> "/movie"), Option(s.get))
      }
      case e: JsError => {
        e.errors.foreach(println(_))
        val p = for {
          entry <- e.errors
        } yield Json.obj(entry._1.toString.drop(1) -> entry._2.head.message)
        println(JsArray(p))
        (false, Json.obj("validation" -> false, "errorList" -> JsArray(p)), None)
      }
    }
  }
}