package dao

import model.AudioBook
import com.mongodb.casbah.MongoClient
import com.mongodb.DBObject
import com.mongodb.util.JSON
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.Play

object AudioBookDao {

  val mongoDbHost = Play.current.configuration.getString("mongodb.host").get
  val mongoDbPort = Play.current.configuration.getInt("mongodb.port").get
  val mongoDbDatabase = Play.current.configuration.getString("mongodb.media.db").get
  val mongoDbAudioCollection = Play.current.configuration.getString("mongodb.media.audio.collection").get

  val client = MongoClient(mongoDbHost, mongoDbPort)
  val db = client(mongoDbDatabase)
  val audioColl = db(mongoDbAudioCollection)

  def add(audioBook: AudioBook): AudioBook = {
    println(s"Adding new audio book $audioBook")
    val audioBookJson = AudioBook.toJson(audioBook)
    val dbObject: DBObject = JSON.parse(audioBookJson.toString).asInstanceOf[DBObject]

    audioColl.insert(dbObject)

    val mongoId = dbObject.get("_id")
    val updatedAudioBook = audioBook.copy(id = Option(mongoId.toString()))

    // synchronize the MongoDb object with the new id.
    update(updatedAudioBook)
    updatedAudioBook
  }

  def update(audioBook: AudioBook) {
    println(s"Updating audio book $audioBook")
    val audioBookJson = AudioBook.toJson(audioBook)
    val dbObject: DBObject = JSON.parse(audioBookJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(audioBook.id.get)
    val query = MongoDBObject("_id" -> objectId)

    audioColl.findAndModify(query, dbObject)
  }

  def findAll(): Seq[AudioBook] = {
    val results = audioColl.find().sort(MongoDBObject("title" -> 1))
    val audioBooks = results.map(dbObjectToAudioBook(_).get)
    audioBooks.toSeq
  }

  def recent(): Seq[AudioBook] = {
    val results = audioColl.find().sort(MongoDBObject("id" -> -1)).limit(10)
    results.map(dbObjectToAudioBook(_).get).toSeq
  }

  def findById(id: String): Option[AudioBook] = {
    try {
      val audioBookOption: Option[audioColl.T] = audioColl.findOneByID(new ObjectId(id))
      audioBookOption match {
        case None => None
        case Some(_) => {
          val audioBook = audioBookOption.get

          Json.parse(audioBook.toString()).validate[AudioBook] match {
            case s: JsSuccess[AudioBook] => Option(s.get)
            case e: JsError => None
          }
        }
      }
    } catch {
      case e: IllegalArgumentException => {
        println(s"Could not find audio book: $e.getMessage()")
        None
      }
    }
  }

  private def dbObjectToAudioBook(dbObject: DBObject): Option[AudioBook] = {
    Json.parse(dbObject.toString()).validate[AudioBook] match {
      case s: JsSuccess[AudioBook] => Option(s.get)
      case e: JsError => None
    }
  }

  def delete(id: String) {
    val audioBookOption: Option[audioColl.T] = audioColl.findOneByID(new ObjectId(id))
    audioColl.remove(audioBookOption.get)
  }
}