package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import dispatch._
import dispatch.Defaults._
import java.net.URLEncoder
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Success
import scala.util.Failure
import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import model.Movie2
import dao.Movie2Dao
import model.MovieShort
import utils.TheMovieDbWrapper
import dao.ActorDao
import model.Actor

object Movies extends Controller {

  var movies: Seq[Movie2] = Seq.empty

  def init() {
    movies = Movie2Dao.findAll
  }

  def index = Action {
    Ok(views.html.movies.index())
  }

  def list = Action {
    if (movies.isEmpty) {
      movies = Movie2Dao.findAll
    }
    Ok(Json.toJson(movies))
  }

  def title(id: String) = Action {
    Ok(views.html.movies.details(id))
  }

  def movieById(id: String) = Action {
    println(s"request for movie by ID: $id")
    val movie = findById(id)
    println(s"Found movie: $movie")

    Ok(Json.toJson(movie))
  }

  def add = Action(parse.json) { request =>
    val movieJsonString = request.body
    println(s"received add movie request: $movieJsonString")
    val movieJson = Json.toJson(movieJsonString)
    println(s"converted Json: $movieJson")

    val (isValid, jsonResult, movieOption) = validateMovieJson(movieJson)
    if (isValid) {
      movies = movies :+ Movie2Dao.add(movieOption.get)
    }
    Ok(jsonResult)
  }

  def addImdb = Action(parse.json) { request =>
    val movieJsonString = request.body
    println(s"received add movie request for imdb: $movieJsonString")
    val movieJson = Json.toJson(movieJsonString)
    println(s"converted Json: $movieJson")

    val (isValid, jsonResult, movieOption) = validateImdbMovieJson(movieJson)
    if (isValid) {
      val omdbJson = getOmdbJsonById(movieOption.get.imdbId)
      val imdbId = getValue(omdbJson, "imdbID")
      imdbId match {
        case Some(_) => {
          val movie = Movie2.fromOmdb(omdbJson, movieOption.get.folder, movieOption.get.dvd)
          println(Json.prettyPrint(Json.toJson(movie)))
          val dbMovie = Movie2Dao.add(movie)
          movies = movies :+ dbMovie
          Ok(Json.obj("validation" -> true, "redirectPath" -> s"/movies/${dbMovie.id.get}"))
        }
        case None => {
          Ok(Json.obj("validation" -> false, "error" -> getValue(omdbJson, "Error")))
        }
      }
    } else {
      Ok(jsonResult)
    }
  }

  def edit = Action(parse.json) { request =>
    val movieJsonString = request.body
    println(s"received edit movie request: $movieJsonString")
    val movieJson = Json.toJson(movieJsonString)
    println(s"converted Json: $movieJson")

    val (isValid, jsonResult, movieOption) = validateMovieJson(movieJson)
    if (isValid) {
      val validatedMovie = movieOption.get
      Movie2Dao.update(validatedMovie)
      movies = Movie2Dao.findAll
    }
    Ok(jsonResult)
  }

  def findUi = Action { request =>
    val entity = request.getQueryString("entity").getOrElse("unknown")
    val name = request.getQueryString("name").getOrElse("unknown")
    Ok(views.html.movies.find(entity, name))
  }

  def find = Action { request =>
    println(request.queryString)
    val entity = request.getQueryString("entity").getOrElse("invalid")
    println(entity)
    entity match {
      case "Actor" => findByActor(request)
      case "Director" => findByDirector(request)
      case "Writer" => findByWriter(request)
      case "Sort" => sortBy(request)
      case _ => BadRequest("Search action not allowed!")
    }
  }

  private def findByActor(implicit request: RequestHeader) = {
    val actor = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByActor = Movie2Dao.findByActor(actor)
    println("movies by actor: " + moviesByActor)
    Ok(Json.toJson(moviesByActor))
  }

  private def findByDirector(implicit request: RequestHeader) = {
    val director = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByDirector = Movie2Dao.findByDirector(director)
    println("movies by director: " + moviesByDirector)
    Ok(Json.toJson(moviesByDirector))
  }

  private def findByWriter(implicit request: RequestHeader) = {
    val writer = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByWriter = Movie2Dao.findByWriter(writer)
    println("movies by writer: " + moviesByWriter)
    Ok(Json.toJson(moviesByWriter))
  }

  private def sortBy(implicit request: RequestHeader) = {
    val sortBy = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    sortBy match {
      case "time" => Ok(Json.toJson(Movie2Dao.sortByTime))
      case "rating" => Ok(Json.toJson(Movie2Dao.sortByRating))
    }
  }

  private def getValue(omdbDataJsValue: JsValue, tag: String): Option[String] = {
    (omdbDataJsValue \ tag).validate[String] match {
      case s: JsSuccess[String] => Option(s.get)
      case e: JsError => None
    }
  }

  private def validateImdbMovieJson(movieJson: JsValue): (Boolean, JsValue, Option[MovieShort]) = {
    movieJson.validate[MovieShort] match {
      case s: JsSuccess[MovieShort] => {
        (true, Json.obj("validation" -> true, "redirectPath" -> "/movies"), Option(s.get))
      }
      case e: JsError => {
        e.errors.foreach(println(_))
        val p = for {
          entry <- e.errors
        } yield Json.obj(entry._1.toString.drop(1) -> entry._2.head.message)
        println(JsArray(p))
        (false, Json.obj("validation" -> false, "errorList" -> JsArray(p)), None)
      }
    }
  }

  private def validateMovieJson(movieJson: JsValue): (Boolean, JsValue, Option[Movie2]) = {
    movieJson.validate[Movie2] match {
      case s: JsSuccess[Movie2] => {
        (true, Json.obj("validation" -> true, "redirectPath" -> "/movies"), Option(s.get))
      }
      case e: JsError => {
        e.errors.foreach(println(_))
        val p = for {
          entry <- e.errors
        } yield Json.obj(entry._1.toString.drop(1) -> entry._2.head.message)
        println(JsArray(p))
        (false, Json.obj("validation" -> false, "errorList" -> JsArray(p)), None)
      }
    }
  }

  def image(imdbId: String) = Action { implicit request =>
    println(s"imdbId: $imdbId")
    val referrer = request.headers.get("referer")
    referrer match {
      case None => {
        println("No referrer given")
        Redirect(checkImdbForImageUrl(imdbId))
      }
      case s => {
        if (s.get.contains("localhost")) {
          println("referrer is localhost")
          val movie = Movie2Dao.findByImdbId(imdbId)
          println(movie)
          movie match {
            case s: Some[Movie2] => Redirect(validateImageUrl(movie.get.imageUrl.get))
            case None => Redirect(checkImdbForImageUrl(imdbId))
          }

        } else {
          println("referrer is NOT localhost")
          Redirect(checkImdbForImageUrl(imdbId))
        }
      }
    }
  }

  private def validateImageUrl(imageUrl: String): String = {
    imageUrl match {
      case "N/A" => "/assets/images/no-image.jpg"
      case "" => "/assets/images/no-image.jpg"
      case _ => imageUrl
    }
  }

  private def checkImdbForImageUrl(imdbId: String): String = {
    imdbId match {
      case a if (a.startsWith("tt")) => Movie2.getTheMovieDbImageUrl(imdbId).get
      case _ => "/assets/images/no-image.jpg"
    }
  }
  //    val movie = findById(id)
  //    val imdbId = getImdbId(movie)
  //    var omdbJson: JsValue = null
  //    imdbId match {
  //      case Some(_) => {
  //        println(s"got imdb id: $imdbId.get")
  //        omdbJson = getOmdbJsonById(imdbId.get)
  //      }
  //      case None => {
  //        println("did not find imdb ID")
  //        omdbJson = getOmdbJsonByTitle(movie.title)
  //      }
  //    }
  //
  //    var posterUrl: String = ""
  //    val posterUrlOmdb = (omdbJson \ "Poster").validate[String]
  //    posterUrlOmdb match {
  //      case s: JsSuccess[String] => {
  //        val omdb = posterUrlOmdb.get
  //        omdb match {
  //          case "N/A" => posterUrl = "/assets/images/no-image.jpg"
  //          case _ => posterUrl = omdb
  //        }
  //        Redirect(posterUrl)
  //      }
  //      case e: JsError => {
  //        println("Errors: " + JsError.toFlatJson(e).toString())
  //        Redirect("/assets/images/no-image.jpg")
  //      }
  //    }
  //  }

  def imageSmall(imdbId: String) = Action { implicit request =>
    val referrer = request.headers.get("referer")
    referrer match {
      case None => Redirect(checkImdbForThumbnailImageUrl(imdbId))
      case s => {
        if (s.get.contains("localhost")) {
          val movie = Movie2Dao.findByImdbId(imdbId)
          movie match {
            case s: Some[Movie2] => Redirect(validateImageUrl(movie.get.imageUrl.get))
            case None => Redirect(checkImdbForThumbnailImageUrl(imdbId))
          }

        } else {
          Redirect(checkImdbForThumbnailImageUrl(imdbId))
        }
      }
    }
  }

  private def checkImdbForThumbnailImageUrl(imdbId: String): String = {
    imdbId match {
      case a if (a.startsWith("tt")) => TheMovieDbWrapper.getThumbnailMoviePosterUrl(imdbId).get
      case _ => "/assets/images/no-image.jpg"
    }
  }

  def actor(name: String) = Action {
    val dbActor = ActorDao.getByFullName(name)
    dbActor match {
      case Some(_) => Ok(Actor.toJson(dbActor.get))
      case None => {
        val movieDbActor = TheMovieDbWrapper.getActorData(name)
        movieDbActor match {
          case Some(_) => {
            val actor = Actor.fromJson(movieDbActor.get)
            actor match {
              case Some(_) => {
                ActorDao.add(actor.get)
                Ok(Actor.toJson(actor.get))
              }
              case None => Ok(Json.obj("error" -> "Error converting MovieDbActor Json to actor model"))
            }
          }
          case None => Ok(Json.obj("error" -> "No information available"))
        }
      }
    }
  }

  def newForm = Action {
    Ok(views.html.movies.form("NewMovieCtrl", "", "Adding New"))
  }

  def newFormImdb = Action {
    Ok(views.html.movies.imdbform())
  }

  def editForm(id: String) = Action {
    Ok(views.html.movies.form("EditMovieCtrl", id, "Editing"))
  }

  def delete(id: String) = Action {
    Movie2Dao.delete(id);
    movies = Movie2Dao.findAll
    Ok("")
  }

  def getImdbId(movie: Movie2): Option[String] = {
    movie.url match {
      case Some(_) => {
        val url = movie.url.get
        if (url.length() > 10) {
          Some(movie.url.get.split("/")(4))
        } else {
          None
        }
      }
      case None => None
    }
  }

  //  def imdb(id: String) = Action {
  //    val movie = findById(id)
  //    val imdbId = getImdbId(movie)
  //    imdbId match {
  //      case Some(_) => {
  //        println(s"got imdb id: $imdbId.get")
  //        Ok(getOmdbJsonById(imdbId.get))
  //      }
  //      case None => {
  //        println("did not find imdb ID")
  //        Ok(getOmdbJsonByTitle(movie.title))
  //      }
  //    }
  //  }

  def findById(id: String): Movie2 = {
    Movie2Dao.findById(id).get
  }

  private def getOmdbJsonByTitle(title: String): JsValue = {
    val request = url("http://www.omdbapi.com/?plot=full&t=" + URLEncoder.encode(title, "UTF-8"))
    val response = Http(request OK as.String)

    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
    Json.parse(omdbJsonString)
  }

  private def getOmdbJsonById(id: String): JsValue = {
    val request = url("http://www.omdbapi.com/?plot=full&i=" + id)
    val response = Http(request OK as.String)

    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
    Json.parse(omdbJsonString)
  }
}