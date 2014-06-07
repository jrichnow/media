package mongodb

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.conversions.scala._
import com.mongodb.casbah.WriteConcern
import com.mongodb.casbah.Imports._
import model.AudioBook
import play.api.libs.json.Json
import com.mongodb.util.JSON
import com.mongodb.DBObject
import com.mongodb.util.JSON
import com.mongodb.DBCursor
import org.bson.types.ObjectId
import com.novus.salat._
import com.novus.salat.global._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._


object MongoDbTests {

  def main(args: Array[String]) {
    val client = MongoClient("localhost", 27017)
    val db = client("mediaTest")
    val audioColl = db("audio")

    usingPlainMongo(audioColl)
  }

  def usingGrater(audioColl: MongoCollection) {
    val audio = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
    println(s"Book before: $audio");

    val dbAudio = grater[AudioBook].asDBObject(audio)
    audioColl.save(dbAudio)

    val audioGrater = grater[AudioBook].asObject(dbAudio)
    println(s"Book after: $audioGrater");

    //    val updatedAudio = audioGrater.copy(year = 1999)
    //    println(updatedAudio)

    val audioDocs = audioColl.find()
    println(audioDocs)
    for (doc <- audioDocs) {
      println(doc)
      val objectId = doc.get("_id").asInstanceOf[ObjectId]
      val test = audioColl.findOneByID(objectId)
      println(test.get)
    }

    audioColl.remove(dbAudio)
  }

  def usingPlainMongo(audioColl: MongoCollection) {
    val audio = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
    println(s"before: $audio")
    val audio1 = AudioBook.toJson(audio)
    val dbObject: DBObject = JSON.parse(audio1.toString).asInstanceOf[DBObject]

    audioColl.insert(dbObject)
    
    val mongoId = dbObject.get("_id")
    val updatedBook = audio.copy(id = Option(mongoId.toString()), year=2000)
    println(s"after: $updatedBook")

    val audioDocs = audioColl.find()
    println(audioDocs)
    for (doc <- audioDocs) {
      println(doc)
      val objectId = doc.get("_id").asInstanceOf[ObjectId]
      println(objectId)
    }
    
    val audioJson = AudioBook.toJson(updatedBook)
    val dbObjectAfter: DBObject = JSON.parse(audioJson.toString).asInstanceOf[DBObject]
    
    // Update
    val objectId = new ObjectId(updatedBook.id.get)
    val query = MongoDBObject("_id" -> objectId)
    audioColl.findAndModify(query, dbObjectAfter)
    
    println(audioColl.find().size)
    
    // Remove
//    audioColl.remove(dbObjectAfter)
  }
}