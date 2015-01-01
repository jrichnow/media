package controllers

import play.api._
import play.api.mvc._
import utils.FileHandler
import model.File
import play.api.libs.json.Json
import java.io.{ File => JavaFile }
import scala.io.Source
import model.Movie
import model.AudioBook
import dao.RequestsDao
import model.Request

object Admin extends Controller {

  private val logger = Logger("AdminController")
  val backupFolder = Play.current.configuration.getString("backup.folder").get

  def index = Action {
    Ok(views.html.admin.index())
  }

  def requests = Action {
    Ok(Json.toJson(RequestsDao.findAll))
  }

  def requestsUi = Action {
    Ok(views.html.admin.requests())
  }

  def requestUi = Action {
    Ok(views.html.admin.requestform())
  }

  def request = Action(parse.json) { request =>
    val requestJsonString = request.body
    logger.info(s"search request: $requestJsonString")
    val requestObj = Request.fromJson(requestJsonString)
    RequestsDao.add(requestObj.get)
    Ok("Hello")
  }
  
  def deleteRequest(id: String) = Action {
    logger.info(s"request to delete request for id $id")
    RequestsDao.delete(id)
    Redirect(routes.Admin.requestsUi)
  }

  def export(media: String) = Action {
    media match {
      case "audio" => {
        println("exporting audio data ...")
        FileHandler.exportAudio()
      }
      case "movie" => {
        println("exporting movie data ...")
        FileHandler.exportMovies()
      }
      case "actor" => {
        println("exporting actor data ...")
        FileHandler.exportActors()
      }
    }
    Ok(views.html.admin.index())
  }

  def upload() = Action {
    Ok(views.html.admin.upload())
  }

  //  def fileUpload() = Action(parse.temporaryFile) { request =>
  //    request.body.moveTo(new java.io.File("/tmp/whatever.json"))
  //    Ok("File uploaded")
  //  }

  def fileUploadMovie = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { file =>
      val fileName = file.filename
      println(s"received $fileName")
      if (fileName.endsWith(".json")) {
        println("can process...");
        val newFile = new JavaFile("/tmp/" + file.filename)
        if (newFile.exists())
          newFile.delete()
        file.ref.moveTo(newFile)
        val json = Json.parse(Source.fromFile(newFile).getLines.mkString(""))
        val movieJsResult = json.validate[Seq[Movie]]

        Ok("File has been uploaded.")
      } else {
        Ok("Only .json files can be processed.")
      }
    }.getOrElse {
      Ok("Error Uploading file.")
    }
  }

  def fileUploadAudio = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { file =>
      val fileName = file.filename
      println(s"received $fileName")
      if (fileName.endsWith(".json")) {
        println("can process...");
        val newFile = new JavaFile("/tmp/" + file.filename)
        if (newFile.exists())
          newFile.delete()
        file.ref.moveTo(newFile)
        val json = Json.parse(Source.fromFile(newFile).getLines.mkString(""))
        val audioJsResult = json.validate[Seq[AudioBook]]

        Ok("File has been uploaded.")
      } else {
        Ok("Only .json files can be processed.")
      }
    }.getOrElse {
      Ok("Error Uploading file.")
    }
  }

  def file(name: String) = Action {
    Ok.sendFile(new java.io.File(backupFolder + name))
  }

  def delete(name: String) = Action {
    FileHandler.delete(name)
    Redirect(routes.Admin.index)
  }

  def list() = Action {
    val files = FileHandler.getBackupFiles
    val fileList = files.map(file => File(file.getName(), file.length() / 1045 + "kb", file.lastModified()))
    Ok(Json.toJson(fileList))
  }
}