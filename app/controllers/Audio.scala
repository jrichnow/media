package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import model.AudioBook

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
    Ok(validateJson(audioJson))
  }

  implicit val audioBookReads: Reads[AudioBook] = (
    (__ \ "title").read[String] and
    (__ \ "author").read[String] and
    (__ \ "plot").readNullable[String] and
    (__ \ "year").read[Int](min(1950) keepAnd max(2030)) and
    (__ \ "runtime").readNullable[String](maxLength(5)) and
    (__ \ "format").readNullable[String](maxLength(3)) and
    (__ \ "imageUrl").readNullable[String] and
    (__ \ "genre").readNullable[String] and
    (__ \ "folder").read[Int](min(1) keepAnd max(10)) and
    (__ \ "dvd").read[Int](min(1) keepAnd max(200)))(AudioBook.apply _)

  def validateJson(audioJson: JsValue): JsValue = {
    audioJson.validate[AudioBook] match {
      case s: JsSuccess[AudioBook] => {
        Json.obj("validation" -> true, "redirectPath" -> "/audio")
      }
      case e: JsError => {
        e.errors.foreach(println(_))
        val p = for {
          entry <- e.errors
        } yield Json.obj(entry._1.toString.drop(1) -> entry._2.head.message)
        println(JsArray(p))
        
        Json.obj("validation" -> false, "errorList" -> JsArray(p))
      }
    }
  }
  
}