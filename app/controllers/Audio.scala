package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

object Audio extends Controller {
  
  var audios: JsArray = JsArray()

  def index = Action {
    Ok(views.html.audio.index())
  }

  def list = Action {
	 Ok(audios)
  }
  
  def newForm = Action {
    Ok(views.html.audio.newForm())
  }

  def add = Action(parse.json) { request =>
    val audioJsonString = request.body
    println(s"received add audio request: $audioJsonString")
    val audioJson = Json.toJson(audioJsonString)
    println(s"converted Json: $audioJson")
    validateJson(audioJson) match {
      case true => {
        audios = audios.append(audioJson) 
        println(audios)
        Ok("/audio")
      }
      case false => Ok("validation error")
    }
  }
  
  def validateJson(audioJson:JsValue):Boolean = {
    // TODO Validate audio JSON
    true
  }
}