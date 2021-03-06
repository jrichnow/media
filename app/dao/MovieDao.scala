package dao

import org.bson.types.ObjectId
import com.mongodb.DBObject
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.util.JSON
import play.api.Play
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import model.Movie
import com.mongodb.casbah.commons.MongoDBList
import com.mongodb.BasicDBList
import com.mongodb.casbah.MongoDB
import utils.JsonUtil
import play.api.Logger

object MovieDao {

  private val logger = Logger("MovieDao")

  private val mongoDbHost = Play.current.configuration.getString("mongodb.host").get
  private val mongoDbPort = Play.current.configuration.getInt("mongodb.port").get
  private val mongoDbDatabase = Play.current.configuration.getString("mongodb.media.db").get
  private val mongoDbMovieCollection = Play.current.configuration.getString("mongodb.media.movie.collection").get

  private val client = MongoClient(mongoDbHost, mongoDbPort)
  private val db = client(mongoDbDatabase)
  private val movieColl = db(mongoDbMovieCollection)

  def add(movie: Movie): Movie = {
    logger.info(s"Adding new movie $movie")
    val movieJson = Movie.toJson(movie)
    val dbObject = JSON.parse(movieJson.toString).asInstanceOf[DBObject]

    movieColl.insert(dbObject)

    val mongoId = dbObject.get("_id")
    val updatedMovie = movie.copy(id = Option(mongoId.toString()))

    // synchronize the MongoDb object with the new id.
    update(updatedMovie)
    updatedMovie
  }

  def update(movie: Movie) {
    logger.info(s"Updating movie $movie")
    val movieJson = Movie.toJson(movie)
    val dbObject: DBObject = JSON.parse(movieJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(movie.id.get)
    val query = MongoDBObject("_id" -> objectId)

    movieColl.findAndModify(query, dbObject)
  }

  def findAll(): Seq[Movie] = {
    val results = movieColl.find().sort(MongoDBObject("title" -> 1))
    val movies = results.map(dbObjectToMovie(_).get)
    movies.toSeq
  }

  def find(amount: Int): Seq[Movie] = {
    val results = movieColl.find().sort(MongoDBObject("title" -> 1)).limit(amount)
    val movies = results.map(dbObjectToMovie(_).get)
    movies.toSeq
  }

  def recent(): Seq[Movie] = {
    val results = movieColl.find(MongoDBObject("hd" -> 1)).sort(MongoDBObject("id" -> -1)).limit(10)
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def findById(id: String): Option[Movie] = {
    try {
      val movieOption: Option[movieColl.T] = movieColl.findOneByID(new ObjectId(id))
      movieOption match {
        case None => None
        case Some(_) => {
          val movie = movieOption.get

          Json.parse(movie.toString()).validate[Movie] match {
            case s: JsSuccess[Movie] => Option(s.get)
            case e: JsError => None
          }
        }
      }
    } catch {
      case e: IllegalArgumentException => {
        println(s"Could not find movie: $e.getMessage()")
        None
      }
    }
  }

  def findByImdbId(imdbId: String): Option[Movie] = {
    val dbMovieOption: Option[movieColl.T] = movieColl.findOne(MongoDBObject("imdbId" -> imdbId))
    dbMovieOption match {
      case None => None
      case Some(_) => {
        val movieJson = dbMovieOption.get

        Json.parse(movieJson.toString()).validate[Movie] match {
          case s: JsSuccess[Movie] => Option(s.get)
          case e: JsError => None
        }
      }
    }
  }

  def findByActor(actor: String): Seq[Movie] = {
    val results = movieColl.find(MongoDBObject("actors" -> s"$actor".r)).sort(MongoDBObject("year" -> -1))
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def findByDirector(director: String): Seq[Movie] = {
    val results = movieColl.find(MongoDBObject("director" -> s"$director".r)).sort(MongoDBObject("year" -> -1))
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def findByWriter(writer: String): Seq[Movie] = {
    val results = movieColl.find(MongoDBObject("writer" -> s"$writer".r)).sort(MongoDBObject("year" -> -1))
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def getMovieCountForName(entity: String, name: String): Int = {
    movieColl.find(MongoDBObject(entity -> s"$name".r)).count
  }

  def findPartial(entity: String, name: String): Seq[Movie] = {
    val results = movieColl.find(MongoDBObject(entity -> s"$name".r))
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def sortByTime(): Seq[Movie] = {
    val results = movieColl.find().sort(MongoDBObject("_id" -> -1)).limit(500)
    //    val results = movieColl.find(MongoDBObject("hd" -> 1)).sort(MongoDBObject("_id" -> -1)).limit(500)
    //    val results = movieColl.find(MongoDBObject("folder" -> 3)).sort(MongoDBObject("dvd" -> -1)).limit(100)
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def sortByRating(): Seq[Movie] = {
    val results = movieColl.find().sort(MongoDBObject("rating" -> -1, "year" -> -1)).limit(100)
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def groupByYear(): Seq[(Int, Int)] = {
    val sort = MongoDBObject("$sort" -> MongoDBObject("year" -> -1))
    val group = MongoDBObject("$group" -> MongoDBObject(
      "_id" -> MongoDBObject("year" -> "$year"),
      "num" -> MongoDBObject("$sum" -> 1)))

    val pipeline = MongoDBList(sort, group)
    val result: BasicDBList = db.command(MongoDBObject("aggregate" -> "movie", "pipeline" -> pipeline)).get("result").asInstanceOf[BasicDBList]
    println(s"groupby: $result")
    result.toArray().map(entry => dbObjectToTuple(entry.asInstanceOf[DBObject]))
  }

  private def dbObjectToMovie(dbObject: DBObject): Option[Movie] = {
    Json.parse(dbObject.toString()).validate[Movie] match {
      case s: JsSuccess[Movie] => Option(s.get)
      case e: JsError => None
    }
  }

  private def dbObjectToTuple(dbObject: DBObject): (Int, Int) = {
    val dbObjectAsJson = Json.parse(dbObject.toString())
    val year = dbObjectAsJson \ "_id" \ "year"
    val count = dbObjectAsJson \ "num"
    (year.toString().toInt, count.toString().toInt)
  }

  def delete(id: String) {
    val movieOption: Option[movieColl.T] = movieColl.findOneByID(new ObjectId(id))
    movieColl.remove(movieOption.get)
  }

  def shutdown() {
    logger.info("Closing DB connection")
    client.close
  }
}