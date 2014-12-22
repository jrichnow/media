package controllers

import dao.AudioBookDao
import model.AudioBook
import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.Logger

object AudioBooks extends Controller {

  private val logger = Logger("AudioBookController")

  private var audioBooks: Seq[AudioBook] = Seq.empty

  def init() {
    audioBooks = AudioBookDao.findAll
  }

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
    Ok(Json.toJson(audioBooks.find(audio => audio.id.get == id).get))
  }

  def title(title: String) = Action {
    Ok("")
  }

  def newForm = Action {
    Ok(views.html.audio.form("NewAudioCtrl", "", "Adding New"))
  }

  def editForm(id: String) = Action {
    Ok(views.html.audio.form("EditAudioCtrl", id, "Editing"))
  }

  def delete(id: String) = Action {
    AudioBookDao.delete(id);
    audioBooks = AudioBookDao.findAll
    Ok("")
  }

  def recent = Action {
    Ok(views.html.audio.recent())
  }

  def recentList = Action {
    Ok(Json.toJson(AudioBookDao.recent))
  }

  def add = Action(parse.json) { request =>
    val audioJsonString = request.body
    logger.info(s"received add audio request: $audioJsonString")
    val audioJson = Json.toJson(audioJsonString)
    logger.debug(s"converted Json: $audioJson")

    val (isValid, jsonResult, audioBookOption) = validateAudioJson(audioJson)
    if (isValid) {
      audioBooks = audioBooks :+ AudioBookDao.add(audioBookOption.get)
    }
    Ok(jsonResult)
  }

  def edit = Action(parse.json) { request =>
    val audioJsonString = request.body
    logger.info(s"received edit audio request: $audioJsonString")
    val audioJson = Json.toJson(audioJsonString)
    logger.debug(s"converted Json: $audioJson")

    val (isValid, jsonResult, audioBookOption) = validateAudioJson(audioJson)
    if (isValid) {
      val validatedAudioBook = audioBookOption.get
      AudioBookDao.update(validatedAudioBook)
      audioBooks = AudioBookDao.findAll
    }
    Ok(jsonResult)
  }

  def getSize(): Int = {
    audioBooks.size
  }

  private def validateAudioJson(audioJson: JsValue): (Boolean, JsValue, Option[AudioBook]) = {
    audioJson.validate[AudioBook] match {
      case s: JsSuccess[AudioBook] => {
        (true, Json.obj("validation" -> true, "redirectPath" -> "/audio"), Option(s.get))
      }
      case e: JsError => {
        e.errors.foreach(println(_))
        val p = for {
          entry <- e.errors
        } yield Json.obj(entry._1.toString.drop(1) -> entry._2.head.message)
        logger.debug(JsArray(p).toString)
        (false, Json.obj("validation" -> false, "errorList" -> JsArray(p)), None)
      }
    }
  }
}