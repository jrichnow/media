package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import model.AudioBook
import org.bson.types.ObjectId
import dao.AudioBookDao

object AudioBooks extends Controller {

  var audioBooks: Seq[AudioBook] = Seq.empty

  def index = Action {
    Ok(views.html.audio.index())
  }

  def list = Action {
    if (audioBooks.isEmpty) {
      audioBooks = AudioBookDao.findAll
    }
    Ok(Json.toJson(audioBooks))
  }

  def detailsForm(id: String) = Action {
    Ok(views.html.audio.details(id))
  }

  def details(id: String) = Action {
    println(audioBooks)
    Ok(Json.toJson(audioBooks.find(audio => audio.id.get == id).get))
  }

  def title(title: String) = Action {
    Ok("")
  }

  def newForm = Action {
    Ok(views.html.audio.form("NewAudioCtrl", ""))
  }

  def editForm(id: String) = Action {
    Ok(views.html.audio.form("EditAudioCtrl", id))
  }

  def delete(id: String) = Action {
    AudioBookDao.delete(id);
    audioBooks = AudioBookDao.findAll
    Ok("")
  }

  def add = Action(parse.json) { request =>
    val audioJsonString = request.body
    println(s"received add audio request: $audioJsonString")
    val audioJson = Json.toJson(audioJsonString)
    println(s"converted Json: $audioJson")

    val (isValid, jsonResult, audioBookOption) = validateJson(audioJson)
    if (isValid) {
      audioBooks = audioBooks :+ AudioBookDao.add(audioBookOption.get)
    }
    Ok(jsonResult)
  }

  def edit = Action(parse.json) { request =>
    val audioJsonString = request.body
    println(s"received edit audio request: $audioJsonString")
    val audioJson = Json.toJson(audioJsonString)
    println(s"converted Json: $audioJson")

    val (isValid, jsonResult, audioBookOption) = validateJson(audioJson)
    if (isValid) {
      val validatedAudioBook = audioBookOption.get
      AudioBookDao.update(validatedAudioBook)
      audioBooks = AudioBookDao.findAll
    }
    Ok(jsonResult)
  }

  def validateJson(audioJson: JsValue): (Boolean, JsValue, Option[AudioBook]) = {
    audioJson.validate[AudioBook] match {
      case s: JsSuccess[AudioBook] => {
        (true, Json.obj("validation" -> true, "redirectPath" -> "/audio"), Option(s.get))
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