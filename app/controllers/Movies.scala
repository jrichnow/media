package controllers

import play.api._
import play.api.mvc._

object Movies extends Controller {

  def index = Action {
    Ok(views.html.movies.index("Movie"))
  }

}