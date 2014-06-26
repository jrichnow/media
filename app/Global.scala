import play.api._
import scala.xml.XML
import model.Movie
import controllers.Movies
import controllers.AudioBooks

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Movies.init
    AudioBooks.init
  }
}