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

object AudioBookDao {

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val audioColl = db("audio")

  def add(audioBook: AudioBook): AudioBook = {
    println(s"Adding new audio book $audioBook")
    val audioBookJson = AudioBook.toJson(audioBook)
    val dbObject: DBObject = JSON.parse(audioBookJson.toString).asInstanceOf[DBObject]

    audioColl.insert(dbObject)

    val mongoId = dbObject.get("_id")
    audioBook.copy(id = Option(mongoId.toString()))
  }

  def update(audioBook: AudioBook) {
    println(s"Saving new audio book $audioBook")
    val audioBookJson = AudioBook.toJson(audioBook)
    val dbObject: DBObject = JSON.parse(audioBookJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(audioBook.id.get)
    val query = MongoDBObject("_id" -> objectId)

    audioColl.findAndModify(query, dbObject)
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
}