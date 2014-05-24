import play.api._
import scala.xml.XML
import model.Movie
import controllers.Movies

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    println("Reading movies xml fil ...")

    try {
      val xmlMovies = XML.loadFile("/Users/jensr/Temp/20130421_movies.xml")
      val movies = (xmlMovies \ "movie").map(
        movie => Movie((movie \ "@title").text,
          (movie \ "@alternativeTitle").text,
          (movie \ "@originalTitle").text,
          (movie \ "@language").text,
          (movie \ "@subTitle").text,
          (movie \ "@genres").text,
          (movie \ "@url").text,
          (movie \ "@releaseYear").text.toInt,
          (movie \ "locations" \ "location" \ "@folder").text.toInt,
          (movie \ "locations" \ "location" \ "@dvdNumber").text.toInt))
      Movies.init(movies)
      println(s"... finished reading of ${movies.size} movies")
    } catch {
      case e : Throwable => println(e.getMessage())
    }
  }
}