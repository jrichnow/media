import play.api._
import scala.xml.XML
import model.Movie
import controllers.Movies
import controllers.AudioBooks
import controllers.Ebooks

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Movies.init
    AudioBooks.init
    Ebooks.init
  }
}