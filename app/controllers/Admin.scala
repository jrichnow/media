package controllers

import play.api._
import play.api.mvc._
import utils.FileHandler
import model.File
import play.api.libs.json.Json

object Admin extends Controller {
  
  val backupFolder = Play.current.configuration.getString("backup.folder").get

  def index = Action {
    Ok(views.html.admin.index())
  }

  def export(media: String) = Action {
    println("exporting audio data ...")
    FileHandler.exportAudio()
    Ok(views.html.admin.index())
  }
  
  def upload() = Action {
	  Ok(views.html.admin.upload())
  }
  
  def file(name: String) = Action {
    Ok.sendFile(new java.io.File(backupFolder + name))
  }

  def list() = Action {
	val files = FileHandler.getBackupFiles
	val fileList = files.map(file => File(file.getName(), file.length() / 1045 + "kb", file.lastModified()))
    Ok(Json.toJson(fileList))
  }
}