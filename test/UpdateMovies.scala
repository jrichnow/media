

import dao.MovieDao
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import model.Movie
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import dispatch._
import dispatch.Defaults._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.json.JsSuccess
import model.Movie
import com.mongodb.util.JSON
import org.bson.types.ObjectId
import model.Movie

/**
 * TODO Check whether this is still needed.
 */
object UpdateMovies {

  val client = MongoClient("localhost", 27017)
  val db = client("media")
  val movieColl = db("movie")

  def main(args: Array[String]) {
    val results = movieColl.find().sort(MongoDBObject("title" -> 1))
    val movies = results.map(dbObjectToMovie(_).get).toSeq
    for (movie <- movies) {
      println(movie)
      val imdbIdOption = getImdbId(movie)
      imdbIdOption match {
        case Some(_) => {
          val imdbIdFromUrl = imdbIdOption.get
          val omdbDataJsValue = getOmdbJsonById(imdbIdFromUrl)
          // Extract data points
          val imdbId = getValue(omdbDataJsValue, "imdbID")
          val plot = getValue(omdbDataJsValue, "Plot")
          val actors = getValue(omdbDataJsValue, "Actors")
          val writer = getValue(omdbDataJsValue, "Writer")
          val director = getValue(omdbDataJsValue, "Director")
          val runtime = getValue(omdbDataJsValue, "Runtime")
          val rating = getValue(omdbDataJsValue, "imdbRating")
          val ratingResolved: Option[Double] = rating match {
            case Some(_) => {
              rating.get match {
                case "N/A" => None
                case _ => Option(rating.get.toDouble)}
              }
            case None => None
          }
          
          val rated = getValue(omdbDataJsValue, "Rated")

          val posterUrlOmdb = getValue(omdbDataJsValue, "Poster")
          val imgUrl = posterUrlOmdb.getOrElse("/assets/images/no-image.jpg")

          //          println(s"imdbId: $imdbId")
          //          println(s"plot: $plot")
          //          println(s"actors: $actors")
          //          println(s"writer: $writer")
          //          println(s"director: $director")
          //          println(s"runtime: $runtime")
          //          println(s"rating: $rating")
          //          println(s"rated: $rated")
          //          println(s"imgUrl: $imgUrl")

          val newMovie = new Movie(movie.id, movie.title, movie.alternativeTitle, movie.originalTitle, movie.language, movie.subTitle,
            movie.genres, movie.url, movie.year, movie.folder, movie.dvd, movie.hd, imdbId, plot, actors, writer, director, runtime,
            ratingResolved, rated, Option(imgUrl))
          println(Json.prettyPrint(Json.toJson(newMovie)))

          update(newMovie)
        }
        case None => println("no imdb id")
      }
    }
    client.close
  }

  private def getValue(omdbDataJsValue: JsValue, tag: String): Option[String] = {
    (omdbDataJsValue \ tag).validate[String] match {
      case s: JsSuccess[String] => Option(s.get)
      case e: JsError => None
    }
  }

  private def dbObjectToMovie(dbObject: DBObject): Option[Movie] = {
    Json.parse(dbObject.toString()).validate[Movie] match {
      case s: JsSuccess[Movie] => Option(s.get)
      case e: JsError => None
    }
  }

  private def getImdbId(movie: Movie): Option[String] = {
    movie.url match {
      case Some(_) => {
        val url = movie.url.get
        if (url.length() > 10) {
          try {
            Some(movie.url.get.split("/")(4))
          } catch {
            case e: Exception => None
          }
        } else {
          None
        }
      }
      case None => None
    }
  }

  private def getOmdbJsonById(id: String): JsValue = {
    val request = url("http://www.omdbapi.com/?plot=full&i=" + id)
    val response = Http(request OK as.String)

    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
    Json.parse(omdbJsonString)
  }

  def update(movie: Movie) {
    println(s"Updating movie $movie")
    val movieJson = Movie.toJson(movie)
    val dbObject: DBObject = JSON.parse(movieJson.toString).asInstanceOf[DBObject]

    val objectId = new ObjectId(movie.id.get)
    val query = MongoDBObject("_id" -> objectId)

    movieColl.findAndModify(query, dbObject)
  }
}