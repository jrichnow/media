package controllers

import javax.inject.{Inject, Singleton}

import dao.ActorDao
import dao.MovieDao
import model.Actor
import model.Movie
import model.MovieShort
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import play.api.Logger
import utils.OmdbWrapper
import utils.TheMovieDbWrapper

import scala.collection.mutable.Buffer
import scala.collection.mutable.Map
import play.api.libs.json.JsArray
import utils.JsonUtil
import services.MovieService

@Singleton
class Movies @Inject() (actorDao: ActorDao, movieService: MovieService, movieDao: MovieDao, wrapper: TheMovieDbWrapper) extends Controller {

  private val logger = Logger("MovieController")
  private var movies: Seq[Movie] = Seq.empty

  def init() {
    logger.info("initialising controller with all movies from DB")
    movies = movieDao.findAll()
  }

  def index = Action {
    Ok(views.html.movies.index())
  }

  def list = Action {
    logger.info("retrieving movie list.")
    if (movies.isEmpty) {
      movies = movieDao.findAll()
    }
    Ok(Json.toJson(movies))
  }

  def title(id: String) = Action {
    logger.info(s"Getting movie by title $id")
    Ok(views.html.movies.details(id))
  }

  def movieById(id: String) = Action {
    logger.info(s"request for movie by ID: $id")
    val movie = findById(id)
    logger.info(s"Found movie: ${Movie.toStringShort(movie)}")
    movieService.checkPersonDataForMovie(movie)
    Ok(Json.toJson(movie))
  }

  def add() = Action(parse.json) { request =>
    val movieJsonString = request.body
    logger.info(s"received add movie request: $movieJsonString")
    val movieJson = Json.toJson(movieJsonString)
    logger.info(s"converted Json: $movieJson")

    val (isValid, jsonResult, movieOption) = validateMovieJson(movieJson)
    if (isValid) {
      movies = movies :+ movieDao.add(movieOption.get)
    }
    Ok(jsonResult)
  }

  def addImdb() = Action(parse.json) { request =>
    val movieJsonString = request.body
    logger.info(s"add movie request for imdb: $movieJsonString")
    val movieJson = Json.toJson(movieJsonString)
    logger.info(s"converted Json: $movieJson")

    val (isValid, jsonResult, movieOption) = validateImdbMovieJson(movieJson)
    if (isValid) {
      val omdbJson = OmdbWrapper.getOmdbJsonById(movieOption.get.imdbId)
      val imdbId = getValue(omdbJson, "imdbID")
      imdbId match {
        case Some(_) => {
          val movie = OmdbWrapper.fromOmdb(omdbJson, movieOption.get.folder, movieOption.get.dvd, movieOption.get.hd)
          logger.info(Json.prettyPrint(Json.toJson(movie)))
          val dbMovie = movieDao.add(movie)
          movieService.checkPersonDataForMovie(dbMovie)
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

  def edit() = Action(parse.json) { request =>
    val movieJsonString = request.body
    logger.info(s"edit movie request: $movieJsonString")
    val movieJson = Json.toJson(movieJsonString)
    logger.info(s"converted Json: $movieJson")

    val (isValid, jsonResult, movieOption) = validateMovieJson(movieJson)
    if (isValid) {
      val validatedMovie = movieOption.get
      movieDao.update(validatedMovie)
      movies = movieDao.findAll()
    }
    Ok(jsonResult)
  }

  def findUi = Action { request =>
    val entity = request.getQueryString("entity").getOrElse("unknown")
    val name = request.getQueryString("name").getOrElse("unknown")
    logger.info(s"findUI for entity '$entity' and name '$name'")
    Ok(views.html.movies.find(entity, name))
  }

  def find = Action { request =>
    val entity = request.getQueryString("entity").getOrElse("invalid")
    logger.info(s"find ${request.queryString.toString}")
    entity match {
      case "Actor" => findByActor(request)
      case "Director" => findByDirector(request)
      case "Writer" => findByWriter(request)
      case "Sort" => sortBy(request)
      case _ => BadRequest("Search action not allowed!")
    }
  }

  def image(imdbId: String) = Action { implicit request =>
    logger.info(s"image request for imdbId: $imdbId")
    val referrer = request.headers.get("referer")
    referrer match {
      case None => {
        logger.info("No referrer given")
        Redirect(checkImdbForImageUrl(imdbId))
      }
      case s => {
        logger.info(s"referrer is ${s.get}")
        if (s.get.contains("localhost")) {
          val movie = movieDao.findByImdbId(imdbId)
          movie match {
            case s: Some[Movie] => {
              logger.info(Movie.toStringShort(movie.get))
              Redirect(validateImageUrl(movie.get.imageUrl.get))
            }
            case None => Redirect(checkImdbForImageUrl(imdbId))
          }

        } else {
          Redirect(checkImdbForImageUrl(imdbId))
        }
      }
    }
  }

  def imageSmall(imdbId: String) = Action { implicit request =>
    logger.debug(s"thumbnail request for imdbId $imdbId")
    val referrer = request.headers.get("referer")
    referrer match {
      case None => Redirect(checkImdbForThumbnailImageUrl(imdbId))
      case s => {
        if (s.get.contains("localhost")) {
          val movie = movieDao.findByImdbId(imdbId)
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
    logger.info(s"actor request for $name")
    val dbActor = actorDao.getByFullName(name)
    dbActor match {
      case Some(_) => Ok(Actor.toJson(dbActor.get))
      case None => {
        val movieDbActor = wrapper.getPersonData(name)
        movieDbActor match {
          case Some(_) => {
            val actor = Actor.fromJson(movieDbActor.get)
            actor match {
              case Some(_) => {
                actorDao.add(actor.get)
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

  def searchUi = Action {
    Ok(views.html.movies.searchform())
  }

  def search = Action(parse.json) { request =>
    val searchJsonString = request.body
    logger.info(s"search request: $searchJsonString")
    val entity = (searchJsonString \ "entity").as[String]
    val term = (searchJsonString \ "term").as[String]
    entity match {
      case a if a.equals("Name") => {
        val resultMap = searchByName(term)
        Ok(JsonUtil.searchResultByNameToJson(resultMap))
      }
      case b if b.equals("Rating") => {
        logger.info("search by rating")
        Ok(Json.obj("error" -> "Search by rating is not implemented yet"))
      }
      case c if c.equals("Genre") => {
        logger.info("search by genre")
        Ok(Json.obj("error" -> "Search by genre is not implemented yet"))
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
    logger.info(s"request to delete movie for id $id")
    movieDao.delete(id)
    movies = movieDao.findAll()
    Ok("")
  }

  def getSize(): Int = {
    movies.size
  }

  private def searchByName(name: String): Map[Actor, Seq[(String, Int)]] = {
    val actors = actorDao.findPartial(name)
    logger.info(s"Found matching names: $actors")
    val map = Map.empty[Actor, Seq[(String, Int)]]

    val list = Buffer.empty[(String, String, Int)]
    for (actor <- actors) {
      map.put(actor, getSearchResultForName(actor))
    }

    map
    //    val actors = future { movieDao.findByActor(actorNames(0).name) }
    //    val directors = future { movieDao.findPartial(Movie.directorField, name) }
    //    val writers = future { movieDao.findPartial(Movie.writerField, name) }
    //
    //    val result = for {
    //      a <- actors
    //      b <- directors
    //      c <- writers
    //    } yield a ++ b ++ c
    //
    //    Await result (result, 2 seconds)
  }

  private def getSearchResultForName(actor: Actor): Seq[(String, Int)] = {
    val list = Buffer.empty[(String, Int)]

    getMovieCountForNameAndEntity(Movie.actorField, actor.name, list)
    getMovieCountForNameAndEntity(Movie.directorField, actor.name, list)
    getMovieCountForNameAndEntity(Movie.writerField, actor.name, list)

    list
  }

  private def getMovieCountForNameAndEntity(entity: String, name: String, list: Buffer[(String, Int)]) {
    val count = movieDao.getMovieCountForName(entity, name)
    if (count > 0) {
      val tuple = (mapEntityNames(entity), count)
//      println(tuple)
      list += tuple
    } else {
      None
    }
  }

  private def mapEntityNames(entity: String): String = {
    entity match {
      case a if a.equals("actors") => "Actor"
      case b if b.equals("director") => "Director"
      case c if c.equals("writer") => "Writer"
    }
  }

  private def findByActor(implicit request: RequestHeader) = {
    val actor = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByActor = movieDao.findByActor(actor)
    logger.info(s"movies by actor: $moviesByActor")
    Ok(Json.toJson(moviesByActor))
  }

  private def findByDirector(implicit request: RequestHeader) = {
    val director = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByDirector = movieDao.findByDirector(director)
    logger.info(s"movies by director: $moviesByDirector")
    Ok(Json.toJson(moviesByDirector))
  }

  private def findByWriter(implicit request: RequestHeader) = {
    val writer = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    val moviesByWriter = movieDao.findByWriter(writer)
    logger.info(s"movies by writer: $moviesByWriter")
    Ok(Json.toJson(moviesByWriter))
  }

  private def sortBy(implicit request: RequestHeader) = {
    val sortBy = request.queryString.get("name").flatMap(_.headOption).getOrElse("")
    sortBy match {
      case "time" => Ok(Json.toJson(movieDao.sortByTime))
      case "rating" => Ok(Json.toJson(movieDao.sortByRating))
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
//        e.errors.foreach(println(_))
        val p = for {
          entry <- e.errors
        } yield Json.obj(entry._1.toString.drop(1) -> entry._2.head.message)
        logger.info(JsArray(p).toString)
        (false, Json.obj("validation" -> false, "errorList" -> JsArray(p)), None)
      }
    }
  }

  private def validateMovieJson(movieJson: JsValue): (Boolean, JsValue, Option[Movie]) = {
    movieJson.validate[Movie] match {
      case s: JsSuccess[Movie] => {
        (true, Json.obj("validation" -> true, "redirectPath" -> s"/movies/${s.get.id.get}"), Option(s.get))
      }
      case e: JsError => {
//        e.errors.foreach(println(_))
        val p = for {
          entry <- e.errors
        } yield Json.obj(entry._1.toString.drop(1) -> entry._2.head.message)
        logger.info(JsArray(p).toString)
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
      case a if a.startsWith("tt") => wrapper.getThumbnailMoviePosterUrl(imdbId).get
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
    movieDao.findById(id).get
  }
}