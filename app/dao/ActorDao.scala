package dao

import com.mongodb.casbah.MongoClient
import play.api.Play
import model.Actor
import com.mongodb.util.JSON
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import org.bson.types.ObjectId
import play.api.Logger

object ActorDao {
  
  private val logger = Logger("ActorDao")
  
  private val mongoDbHost = Play.current.configuration.getString("mongodb.host").get
  private val mongoDbPort = Play.current.configuration.getInt("mongodb.port").get
  private val mongoDbDatabase = Play.current.configuration.getString("mongodb.media.db").get
  private val mongoDbActorCollection = Play.current.configuration.getString("mongodb.media.actor.collection").get

  private val client = MongoClient(mongoDbHost, mongoDbPort)
  private val db = client(mongoDbDatabase)
  private val actorColl = db(mongoDbActorCollection)

  def add(actor: Actor): Actor = {
    logger.info(s"Adding new actor ${actor.name}")
    val actorDbObject = JSON.parse(Actor.toJson(actor).toString()).asInstanceOf[DBObject]
    
    actorColl.insert(actorDbObject)
    
    val mongoId = actorDbObject.get("_id")
    val updatedActor = actor.copy(id = Option(mongoId.toString()))
    
    update(updatedActor)
    updatedActor
  }
  
  def findAll(): Seq[Actor] = {
    val results = actorColl.find().sort(MongoDBObject("name" -> 1))
    val movies = results.map(dbObjectToActor(_).get)
    movies.toSeq
  }
  
  def update(actor: Actor) {
    logger.info(s"Updating actor $actor")
    val actorJson = Actor.toJson(actor)
    val dbObject: DBObject = JSON.parse(actorJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(actor.id.get)
    val query = MongoDBObject("_id" -> objectId)

    actorColl.findAndModify(query, dbObject)
  }
  
  def getByFullName(fullName: String): Option[Actor] = {
    val dbActorOption = actorColl.findOne(MongoDBObject("name" -> fullName))
    logger.info(s"ActorDao search by $fullName resulted in ${dbActorOption.getOrElse("Not found")}")
    dbActorOption match {
      case None => None
      case Some(_) => {
        val actorJson = dbActorOption.get
        
        Json.parse(actorJson.toString()).validate[Actor] match {
          case s: JsSuccess[Actor] => Option(s.get)
          case e: JsError => None
        }
      }
    }
  }
  
  def findPartial(name: String): Seq[Actor] = {
   val results = actorColl.find(MongoDBObject("name" -> s"$name".r))
    results.map(dbObjectToActor(_).get).toSeq
  }
  
  private def dbObjectToActor(dbObject: DBObject): Option[Actor] = {
    Json.parse(dbObject.toString()).validate[Actor] match {
      case s: JsSuccess[Actor] => Option(s.get)
      case e: JsError => None
    }
  }
  
  def shutdown() {
    logger.info("Closing DB connection")
    client.close
  }
}