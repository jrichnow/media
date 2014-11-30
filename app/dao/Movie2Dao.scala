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
import model.Movie2

object Movie2Dao {

  val mongoDbHost = Play.current.configuration.getString("mongodb.host").get
  val mongoDbPort = Play.current.configuration.getInt("mongodb.port").get
  val mongoDbDatabase = Play.current.configuration.getString("mongodb.media.db").get
  val mongoDbMovieCollection = Play.current.configuration.getString("mongodb.media.movie.collection").get

  val client = MongoClient(mongoDbHost, mongoDbPort)
  val db = client(mongoDbDatabase)
  val movieColl = db(mongoDbMovieCollection)

  def add(movie: Movie2): Movie2 = {
    println(s"Adding new movie $movie")
    val movieJson = Movie2.toJson(movie)
    val dbObject: DBObject = JSON.parse(movieJson.toString).asInstanceOf[DBObject]

    movieColl.insert(dbObject)

    val mongoId = dbObject.get("_id")
    val updatedMovie = movie.copy(id = Option(mongoId.toString()))

    // synchronize the MongoDb object with the new id.
    update(updatedMovie)
    updatedMovie
  }

  def update(movie: Movie2) {
    println(s"Updating movie $movie")
    val movieJson = Movie2.toJson(movie)
    val dbObject: DBObject = JSON.parse(movieJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(movie.id.get)
    val query = MongoDBObject("_id" -> objectId)

    movieColl.findAndModify(query, dbObject)
  }

  def findAll(): Seq[Movie2] = {
    val results = movieColl.find().sort(MongoDBObject("title" -> 1))
    val movies = results.map(dbObjectToMovie(_).get)
    movies.toSeq
  }

  def find(amount: Int): Seq[Movie2] = {
    val results = movieColl.find().sort(MongoDBObject("title" -> 1)).limit(amount)
    val movies = results.map(dbObjectToMovie(_).get)
    movies.toSeq
  }

  def recent(): Seq[Movie2] = {
    val results = movieColl.find().sort(MongoDBObject("id" -> -1)).limit(10)
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def findById(id: String): Option[Movie2] = {
    try {
      val movieOption: Option[movieColl.T] = movieColl.findOneByID(new ObjectId(id))
      movieOption match {
        case None => None
        case Some(_) => {
          val movie = movieOption.get

          Json.parse(movie.toString()).validate[Movie2] match {
            case s: JsSuccess[Movie2] => Option(s.get)
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

  def findByImdbId(imdbId: String): Option[Movie2] = {
    val dbMovieOption: Option[movieColl.T] = movieColl.findOne(MongoDBObject("imdbId" -> "tt0499603"))
    dbMovieOption match {
      case None => None
      case Some(_) => {
        val movie = dbMovieOption.get

        Json.parse(movie.toString()).validate[Movie2] match {
          case s: JsSuccess[Movie2] => Option(s.get)
          case e: JsError => None
        }
      }
    }
  }

  def findByActor(actor: String): Seq[Movie2] = {
    val results = movieColl.find(MongoDBObject("actors" -> s"$actor".r)).sort(MongoDBObject("year" -> -1))
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def findByDirector(director: String): Seq[Movie2] = {
    val results = movieColl.find(MongoDBObject("director" -> s"$director".r)).sort(MongoDBObject("year" -> -1))
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def findByWriter(writer: String): Seq[Movie2] = {
    val results = movieColl.find(MongoDBObject("writer" -> s"$writer".r)).sort(MongoDBObject("year" -> -1))
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def sortByTime(): Seq[Movie2] = {
    //    val results = movieColl.find().sort(MongoDBObject("_id" -> -1)).limit(100)
    val results = movieColl.find(MongoDBObject("folder" -> 3)).sort(MongoDBObject("dvd" -> -1)).limit(100)
    results.map(dbObjectToMovie(_).get).toSeq
  }

  def sortByRating(): Seq[Movie2] = {
    val results = movieColl.find().sort(MongoDBObject("rating" -> -1)).limit(100)
    results.map(dbObjectToMovie(_).get).toSeq
  }

  private def dbObjectToMovie(dbObject: DBObject): Option[Movie2] = {
    Json.parse(dbObject.toString()).validate[Movie2] match {
      case s: JsSuccess[Movie2] => Option(s.get)
      case e: JsError => None
    }
  }

  def delete(id: String) {
    val movieOption: Option[movieColl.T] = movieColl.findOneByID(new ObjectId(id))
    movieColl.remove(movieOption.get)
  }
}