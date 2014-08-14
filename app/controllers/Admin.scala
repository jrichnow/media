package controllers

import play.api._
import play.api.mvc._
import utils.FileHandler
import model.File
import play.api.libs.json.Json
import java.io.{ File => JavaFile }
import scala.io.Source

object Admin extends Controller {

  val backupFolder = Play.current.configuration.getString("backup.folder").get

  def index = Action {
    Ok(views.html.admin.index())
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
  
  
  def fileUpload = Action(parse.multipartFormData) { request =>
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
        println(json)
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