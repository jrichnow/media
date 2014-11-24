package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.home(Movies.movies.size, AudioBooks.audioBooks.size, Ebooks.ebooks.size))
  }
  
  def adscale = Action(BodyParsers.parse.json) { request =>
    println(request.body)
    println(request.charset)
    println(request.contentType)
    
    Ok("hi")
  }
}