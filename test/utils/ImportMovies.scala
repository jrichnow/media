package utils

import scala.xml.XML
import model.Movie
import dao.MovieDao
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import com.mongodb.casbah.MongoClient
import play.api.test.FakeApplication

class ImportMovies extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTest"

  val client = MongoClient("localhost", 27017)
  val db = client(testDbName)
  val movieColl = db("movie")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "FileHandler" should {
    "import" in {
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
        for (movie <- movies) {
          MovieDao.add(movie)
        }
      } catch {
        case e: Throwable => println(e.getMessage(), e)
      }
    }
  }
}