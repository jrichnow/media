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
        movie => Movie(
            title = (movie \ "@title").text,
            alternativeTitle = Some((movie \ "@alternativeTitle").text),
            originalTitle = Some((movie \ "@originalTitle").text),
            language = Some((movie \ "@language").text),
          subTitle = Some((movie \ "@subTitle").text),
          genres = Some((movie \ "@genres").text.split(", ")),
          url = Some((movie \ "@url").text),
          year = (movie \ "@releaseYear").text.toInt,
          folder = (movie \ "locations" \ "location" \ "@folder").text.toInt,
          dvd = (movie \ "locations" \ "location" \ "@dvdNumber").text.toInt))
      Movies.init(movies)
      println(s"... finished reading of ${movies.size} movies")
    } catch {
      case e : Throwable => println(e.getMessage())
    }
  }
}