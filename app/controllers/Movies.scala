package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import model.Movie
import scala.collection.immutable.Seq

object Movies extends Controller {
  
  var movies:Seq[Movie] = _

  def index = Action {
    Ok(views.html.movies.index("Movie"))
  }
  
  def list = Action {
    val movieListAsJson = Json.toJson(movies);
    Ok(movieListAsJson)
  }
  
  def init(moviesSeq:Seq[Movie]):Unit = {
    movies = moviesSeq
  }
}