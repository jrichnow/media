package controllers

import play.api._
import play.api.mvc._

object Audio extends Controller {

  def index = Action {
    Ok(views.html.audio.index())
  }
  
  def newForm = Action {
	  Ok(views.html.audio.newForm())
  }

}