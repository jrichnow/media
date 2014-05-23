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

object Movies extends Controller {

  var movies: Seq[Movie] = _

  def index = Action {
    Ok(views.html.movies.index())
  }

  def list = Action {
    Ok(Json.toJson(movies))
  }

  def title(title: String) = Action {
    Ok(views.html.movies.details(findByTitle(title)))
  }

  def image(title: String) = Action {
    val request = url("http://www.omdbapi.com/?t=" + URLEncoder.encode(title, "UTF-8"))
    val response = Http(request OK as.String)

    val extractedLocalValue = Await.result(response, Duration(10, "s"))
    val omdbJson = Json.parse(extractedLocalValue)
    println(omdbJson)
    var posterUrl: String = ""
    val posterUrlOmdb = (omdbJson \ "Poster").validate[String]
    posterUrlOmdb match {
      case s: JsSuccess[String] => {
        posterUrl = posterUrlOmdb.get
        Redirect(posterUrl)
      }
      case e: JsError => {
        println("Errors: " + JsError.toFlatJson(e).toString())
        Redirect("/assets/images/no-image.jpg")
      }
    }
  }

  def imdb(title: String) = Action {
    val request = url("http://www.omdbapi.com/?t=" + URLEncoder.encode(title, "UTF-8"))
    val response = Http(request OK as.String)

    val extractedLocalValue = Await.result(response, Duration(10, "s"))
    val omdbJson = Json.parse(extractedLocalValue)
    println(omdbJson)
    Ok(omdbJson)
  }

  def init(moviesSeq: Seq[Movie]): Unit = {
    movies = moviesSeq
  }

  def findByTitle(title: String): Movie = {
    movies.find(movie => movie.title == title).get
  }
}