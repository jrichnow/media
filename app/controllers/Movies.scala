package controllers

import dao.ActorDao
import dao.MovieDao
import model.Actor
import model.Movie
import model.MovieShort
import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import utils.OmdbWrapper
import utils.TheMovieDbWrapper

object Movies extends Controller {

  var movies: Seq[Movie] = Seq.empty

  def init() {
    movies = MovieDao.findAll
  }

  def index = Action {
    Ok(views.html.movies.index())
  }

  def list = Action {
    if (movies.isEmpty) {
      movies = MovieDao.findAll
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
      movies = movies :+ MovieDao.add(movieOption.get)
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
      val omdbJson = OmdbWrapper.getOmdbJsonById(movieOption.get.imdbId)
      val imdbId = getValue(omdbJson, "imdbID")
      imdbId match {
        case Some(_) => {
          val movie = OmdbWrapper.fromOmdb(omdbJson, movieOption.get.folder, movieOption.get.dvd)
          println(Json.prettyPrint(Json.toJson(movie)))
          val dbMovie = MovieDao.add(movie)
          // TODO Get actor etc info to store in DB
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
      MovieDao.update(validatedMovie)
      movies = MovieDao.findAll
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
          val movie = MovieDao.findByImdbId(imdbId)
          println(movie)
          movie match {
            case s: Some[Movie] => Redirect(validateImageUrl(movie.get.imageUrl.get))
            case None => Redirect(checkImdbForImageUrl(imdbId))
          }

        } else {
          println("referrer is NOT localhost")
          Redirect(checkImdbForImageUrl(imdbId))
        }
      }
    }
  }

  def imageSmall(imdbId: String) = Action { implicit request =>
    val referrer = request.headers.get("referer")
    referrer match {
      case None => Redirect(checkImdbForThumbnailImageUrl(imdbId))
      case s => {
        if (s.get.contains("localhost")) {
          val movie = MovieDao.findByImdbId(imdbId)
          movie match {
            case s: Some[Movie] => Redirect(validateImageUrl(movie.get.imageUrl.get))
            case None => Redirect(checkImdbForThumbnailImageUrl(imdbId))
          }

        } else {
          Redirect(checkImdbForThumbnailImageUrl(imdbId))
        }
      }
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
    MovieDao.delete(id);
    movies = MovieDao.findAll
    Ok("")
  }

  private def findByActor(implicit request: RequestHeader) = {
    val actor = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByActor = MovieDao.findByActor(actor)
    println("movies by actor: " + moviesByActor)
    Ok(Json.toJson(moviesByActor))
  }

  private def findByDirector(implicit request: RequestHeader) = {
    val director = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByDirector = MovieDao.findByDirector(director)
    println("movies by director: " + moviesByDirector)
    Ok(Json.toJson(moviesByDirector))
  }

  private def findByWriter(implicit request: RequestHeader) = {
    val writer = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByWriter = MovieDao.findByWriter(writer)
    println("movies by writer: " + moviesByWriter)
    Ok(Json.toJson(moviesByWriter))
  }

  private def sortBy(implicit request: RequestHeader) = {
    val sortBy = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    sortBy match {
      case "time" => Ok(Json.toJson(MovieDao.sortByTime))
      case "rating" => Ok(Json.toJson(MovieDao.sortByRating))
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

  private def validateMovieJson(movieJson: JsValue): (Boolean, JsValue, Option[Movie]) = {
    movieJson.validate[Movie] match {
      case s: JsSuccess[Movie] => {
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

  private def validateImageUrl(imageUrl: String): String = {
    imageUrl match {
      case "N/A" => "/assets/images/no-image.jpg"
      case "" => "/assets/images/no-image.jpg"
      case _ => imageUrl
    }
  }

  private def checkImdbForImageUrl(imdbId: String): String = {
    imdbId match {
      case a if (a.startsWith("tt")) => Movie.getTheMovieDbImageUrl(imdbId).get
      case _ => "/assets/images/no-image.jpg"
    }
  }

  private def checkImdbForThumbnailImageUrl(imdbId: String): String = {
    imdbId match {
      case a if (a.startsWith("tt")) => TheMovieDbWrapper.getThumbnailMoviePosterUrl(imdbId).get
      case _ => "/assets/images/no-image.jpg"
    }
  }

  private def getImdbId(movie: Movie): Option[String] = {
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

  private def findById(id: String): Movie = {
    MovieDao.findById(id).get
  }
}