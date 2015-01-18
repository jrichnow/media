package dao

import play.api.Logger
import com.mongodb.casbah.MongoClient
import play.api.Play
import model.Request
import com.mongodb.DBObject
import com.mongodb.util.JSON
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.Json

object RequestsDao {

  private val logger = Logger("RequestsDao")

  private val mongoDbHost = Play.current.configuration.getString("mongodb.host").get
  private val mongoDbPort = Play.current.configuration.getInt("mongodb.port").get
  private val mongoDbDatabase = Play.current.configuration.getString("mongodb.media.db").get
  private val mongoDbRequestsCollection = Play.current.configuration.getString("mongodb.media.requests.collection").get

  private val client = MongoClient(mongoDbHost, mongoDbPort)
  private val db = client(mongoDbDatabase)
  private val requestsColl = db(mongoDbRequestsCollection)

  def add(request: Request): Request = {
    logger.info(s"Adding new request $request")
    val requestJson = Request.toJson(request)
    val dbObject = JSON.parse(requestJson.toString).asInstanceOf[DBObject]

    requestsColl.insert(dbObject)

    val mongoId = dbObject.get("_id")
    val updatedRequest = request.copy(id = Option(mongoId.toString()))

    // synchronize the MongoDb object with the new id.
    update(updatedRequest)
    updatedRequest
  }

  def update(request: Request) {
    logger.info(s"Updating request $request")
    val requestJson = Request.toJson(request)
    val dbObject: DBObject = JSON.parse(requestJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(request.id.get)
    val query = MongoDBObject("_id" -> objectId)

    requestsColl.findAndModify(query, dbObject)
  }
  
  def findAll(): Seq[Request] = {
    val results = requestsColl.find().sort(MongoDBObject("id" -> -1))
    val requests = results.map(dbObjectToRequest(_).get)
    requests.toSeq
  }

  def findById(id: String): Option[Request] = {
    println(s"findById $id")
    val requestOption: Option[requestsColl.T] = requestsColl.findOneByID(new ObjectId(id))
    println(s"search result $requestOption")
    requestOption match {
      case None => None
      case Some(_) => {
        val request = requestOption.get
        println(s"option $request")

        Json.parse(request.toString()).validate[Request] match {
          case s: JsSuccess[Request] => Option(s.get)
          case e: JsError => {
            println(e)
            None}
        }
      }
    }
  }
  
  def delete(id: String) {
    val requestOption: Option[requestsColl.T] = requestsColl.findOneByID(new ObjectId(id))
    requestsColl.remove(requestOption.get)
  }

  private def dbObjectToRequest(dbObject: DBObject): Option[Request] = {
    Json.parse(dbObject.toString()).validate[Request] match {
      case s: JsSuccess[Request] => Option(s.get)
      case e: JsError => None
    }
  }
}