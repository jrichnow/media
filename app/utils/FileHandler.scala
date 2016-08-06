package utils

import model.AudioBook
import java.io.File
import java.io.PrintWriter
import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.Configuration
import dao.AudioBookDao

import scala.io.Source
import org.joda.time.format._
import org.joda.time.DateTime
import dao.MovieDao
import dao.ActorDao

@Singleton
class FileHandler @Inject()(configuration: Configuration, audioDao: AudioBookDao, movieDao: MovieDao, actorDao: ActorDao) {

  val backupFolder = configuration.getString("backup.folder").get
  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd_hh-mm")

  def exportAudio(): String = {
    val fileName = backupFolder + "audio_" + dateFormat.print(new DateTime) + ".json"
    val pw = new PrintWriter(new File(fileName))
    pw.print(Json.prettyPrint(Json.toJson(audioDao.findAll())))
    pw.close()

    fileName
  }

  def exportMovies(): String = {
    val fileName = backupFolder + "movie_" + dateFormat.print(new DateTime) + ".json"
    val pw = new PrintWriter(new File(fileName))
    pw.print(Json.prettyPrint(Json.toJson(movieDao.findAll())))
    pw.close()

    fileName
  }

  def exportActors(): String = {
    val fileName = backupFolder + "actor_" + dateFormat.print(new DateTime) + ".json"
    val pw = new PrintWriter(new File(fileName))
    pw.print(Json.prettyPrint(Json.toJson(actorDao.findAll())))
    pw.close()

    fileName
  }

  def importAudio(fileName: String) {
    val audioJsonString = Source.fromFile(fileName).getLines().toList.mkString("")
    val audioJson = Json.parse(audioJsonString)

    val bookList = audioJson.as[List[AudioBook]]
    for (audioBook <- bookList) {
      audioDao.add(audioBook)
    }
  }

  def getBackupFiles(): Array[File] = {
    new File(backupFolder).listFiles()
  }

  def delete(fileName: String) {
    new File(backupFolder + fileName).delete()
  }
}