package services

import dao.ActorDao
import utils.TheMovieDbWrapper
import play.api.Logger
import model.Actor

object MovieService {
  
  private val logger = Logger("MovieService")

  private def checkAndGetPersonData(name: String) {
    val dbActor = ActorDao.getByFullName(name.trim())
    dbActor match {
      case None => {
        val movieDbActor = TheMovieDbWrapper.getActorData(name)
        movieDbActor match {
          case None => logger.info(s"no actor data for name found from MovieDb")
          case a => {
            val actor = ActorDao.add(Actor.fromJson(a.get).get)
            logger.info(s"new actor added to db: ${actor.name}")
          }
        }
      }
      case a => logger.info(s"actor ${a.get.name} exists already")
    }
  }
}