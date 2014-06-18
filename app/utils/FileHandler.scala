package utils

import model.AudioBook
import java.io.File
import java.io.PrintWriter
import play.api.libs.json.Json
import play.api.Play
import dao.AudioBookDao
import scala.io.Source

object FileHandler {
  
  val backupFolder = Play.current.configuration.getString("backup.folder").get

  def exportAudio() {
    val pw = new PrintWriter(new File(backupFolder + "audio-backup.json"))
    pw.print(Json.prettyPrint(Json.toJson(AudioBookDao.findAll)))
    pw.close()
  }
  
  def importAudio() {
	  val beforeLines = Source.fromFile(backupFolder + "audio-backup.json").getLines().toList
	  for (line <- beforeLines) {
	    println("-- " + line)
	  }
  }
}