package services

import dao.ActorDao
import utils.TheMovieDbWrapper
import play.api.Logger
import model.Actor
import model.Movie
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

object MovieService {

  private val logger = Logger("MovieService")

  def checkPersonDataForMovie(movie: Movie) {
    checkPersonsByEntity(movie.actors)
    checkPersonsByEntity(movie.writer)
    checkPersonsByEntity(movie.director)
  }

  private def checkPersonsByEntity(names: Option[String]) {
    val writersFuture = future { checkPersons(names) }
    writersFuture.onComplete {
      case Success(value) => logger.info(s"Successfully completed writer search for ${names}")
      case Failure(error) => logger.info(s"Writer search ${names} resulted in an error: $error")
    }
  }

  private def checkPersons(writers: Option[String]) {
    writers match {
      case None => println("nothing to check")
      case person => {
        val personsArray = person.get.split(", ")
        for (p <- personsArray) {
          if (p.contains("(")) {
            val array = p.split(" \\(")
            checkAndGetPersonData(array(0))
          } else {
            checkAndGetPersonData(p)
          }
        }
      }
    }
  }

  private def checkAndGetPersonData(name: String) {
    val dbActor = ActorDao.getByFullName(name.trim())
    dbActor match {
      case None => {
        val movieDbActor = TheMovieDbWrapper.getPersonData(name)
        movieDbActor match {
          case None => logger.info(s"no person data for name found from MovieDb")
          case a => {
            val actor = ActorDao.add(Actor.fromJson(a.get).get)
            logger.info(s"new person added to db: ${actor.name}")
          }
        }
      }
      case a => logger.info(s"person ${a.get.name} exists already")
    }
  }
}