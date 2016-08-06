import controllers.AudioBooks
import controllers.Ebooks
import controllers.Movies
import dao.ActorDao
import dao.AudioBookDao
import dao.MovieDao
import play.api.Application
import play.api.GlobalSettings
import utils.TheMovieDbWrapper

object Global extends GlobalSettings {

  override def onStart(app: Application) {
//    Movies.init
//    AudioBooks.init
//    Ebooks.init
//    TheMovieDbWrapper.init
  }
  
  override def onStop(app: Application) {
//    MovieDao.shutdown
//    ActorDao.shutdown
//    AudioBookDao.shutdown
  }
}