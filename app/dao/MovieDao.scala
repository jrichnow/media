package dao

import org.bson.types.ObjectId

import com.mongodb.DBObject
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.util.JSON

import model.Movie
import play.api.Play
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json

object MovieDao {

  val mongoDbHost = Play.current.configuration.getString("mongodb.host").get
  val mongoDbPort = Play.current.configuration.getInt("mongodb.port").get
  val mongoDbDatabase = Play.current.configuration.getString("mongodb.media.db").get
  val mongoDbMovieCollection = Play.current.configuration.getString("mongodb.media.movie.collection").get

  val client = MongoClient(mongoDbHost, mongoDbPort)
  val db = client(mongoDbDatabase)
  val movieColl = db(mongoDbMovieCollection)

  def add(movie: Movie): Movie = {
    println(s"Adding new movie $movie")
    val movieJson = Movie.toJson(movie)
    val dbObject: DBObject = JSON.parse(movieJson.toString).asInstanceOf[DBObject]

    movieColl.insert(dbObject)

    val mongoId = dbObject.get("_id")
    val updatedMovie = movie.copy(id = Option(mongoId.toString()))

    // synchronize the MongoDb object with the new id.
    update(updatedMovie)
    updatedMovie
  }

  def update(movie: Movie) {
    println(s"Updating movie $movie")
    val movieJson = Movie.toJson(movie)
    val dbObject: DBObject = JSON.parse(movieJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(movie.id.get)
    val query = MongoDBObject("_id" -> objectId)

    movieColl.findAndModify(query, dbObject)
  }

  def findAll(): Seq[Movie] = {
    val results = movieColl.find()
    val movies = results.map(dbObjectToMovie(_).get)
    movies.toSeq
  }

  def recent(): Seq[Movie] = {
    val results = movieColl.find().sort(MongoDBObject("id" -> -1)).limit(10)
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

  private def dbObjectToMovie(dbObject: DBObject): Option[Movie] = {
    Json.parse(dbObject.toString()).validate[Movie] match {
      case s: JsSuccess[Movie] => Option(s.get)
      case e: JsError => None
    }
  }

  def delete(id: String) {
    val movieOption: Option[movieColl.T] = movieColl.findOneByID(new ObjectId(id))
    movieColl.remove(movieOption.get)
  }
}