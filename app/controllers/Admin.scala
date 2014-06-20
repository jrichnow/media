package controllers

import play.api._
import play.api.mvc._
import utils.FileHandler

object Admin extends Controller {

  def index = Action {
    Ok(views.html.admin.index())
  }
  
  def export(media: String) = Action {
    println("exporting audio data ...")
    FileHandler.exportAudio()
    Ok(views.html.admin.index())
  }
}