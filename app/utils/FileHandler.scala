package utils

import model.AudioBook
import java.io.File
import java.io.PrintWriter
import play.api.libs.json.Json
import play.api.Play
import dao.AudioBookDao
import scala.io.Source
import play.api.libs.json.JsArray
import org.joda.time.format._
import org.joda.time.DateTime
import dao.MovieDao
import dao.ActorDao

object FileHandler {

  val backupFolder = Play.current.configuration.getString("backup.folder").get
  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd_hh-mm")

  def exportAudio(): String = {
    val fileName = backupFolder + "audio_" + dateFormat.print(new DateTime) + ".json"
    val pw = new PrintWriter(new File(fileName))
    pw.print(Json.prettyPrint(Json.toJson(AudioBookDao.findAll)))
    pw.close()

    fileName
  }

  def exportMovies(): String = {
    val fileName = backupFolder + "movie_" + dateFormat.print(new DateTime) + ".json"
    val pw = new PrintWriter(new File(fileName))
    pw.print(Json.prettyPrint(Json.toJson(MovieDao.findAll)))
    pw.close()

    fileName
  }

  def exportActors(): String = {
    val fileName = backupFolder + "actor_" + dateFormat.print(new DateTime) + ".json"
    val pw = new PrintWriter(new File(fileName))
    pw.print(Json.prettyPrint(Json.toJson(ActorDao.findAll)))
    pw.close()

    fileName
  }

  def importAudio(fileName: String) {
    val audioJsonString = Source.fromFile(fileName).getLines().toList.mkString("")
    val audioJson = Json.parse(audioJsonString)

    val bookList = audioJson.as[List[AudioBook]]
    for (audioBook <- bookList) {
      AudioBookDao.add(audioBook)
    }
  }

  def getBackupFiles(): Array[File] = {
    new File(backupFolder).listFiles()
  }

  def delete(fileName: String) {
    new File(backupFolder + fileName).delete()
  }
}