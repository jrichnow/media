package mongodb

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec

import com.mongodb.casbah.MongoClient

import dao.Movie2Dao
import model.Movie2
import play.api.test.FakeApplication

class Movie2DaoSpec extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTest"

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val movieColl = db("movie")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "MovieDao" should {

    "return no movie when collection is empty" in {
      movieColl.drop()
      Movie2Dao.findAll.size mustEqual (0)

      val movieOption = Movie2Dao.findById("5393ed7dc0260baea0990019")
      movieOption mustBe None
    }

    "add a movie" in {
      movieColl.drop()
      val movie = Movie2(title = "Dreamland", year = 2012, folder = 1, dvd = 2)
      movie.id mustBe None

      val updatedMovie = Movie2Dao.add(movie)

      // Check that we have a new MongoDb id.
      updatedMovie.id.get.length() >= 6
      updatedMovie.year must equal(2012)

      val dbMovieOption = Movie2Dao.findById(updatedMovie.id.get)
      val dbMovie = dbMovieOption.get
      dbMovie.year must equal(2012)

      dbMovie.id.get mustEqual (updatedMovie.id.get)

      Movie2Dao.findAll.size mustEqual (1)
    }

    "update a movie" in {
      movieColl.drop()
      val movie = Movie2(title = "Dreamland", year = 2012, folder = 1, dvd = 2)
      val updatedMovie = Movie2Dao.add(movie)
      updatedMovie.year must equal(2012)

      // update.
      val modifiedMovie = updatedMovie.copy(year = 2000)
      Movie2Dao.update(modifiedMovie)

      val modifiedDbMovie = Movie2Dao.findById(updatedMovie.id.get).get

      modifiedMovie.year must equal(2000)
      modifiedMovie.id === updatedMovie.id

      Movie2Dao.findAll.size mustEqual (1)
    }

    "delete a movie" in {
      movieColl.drop()
      val movie = Movie2(title = "Dreamland", year = 2012, folder = 1, dvd = 2)
      val updatedMovie = Movie2Dao.add(movie)

      Movie2Dao.findAll.size mustEqual (1)

      Movie2Dao.delete(updatedMovie.id.get)

      Movie2Dao.findAll.size mustEqual (0)
    }

    "return a limited list of recent movies" in {
      movieColl.drop()
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2000, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2001, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2002, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2003, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2004, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2005, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2006, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2007, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2008, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2009, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2010, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2011, folder = 1, dvd = 2))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2012, folder = 1, dvd = 2))

      val recentMovies = Movie2Dao.recent

      recentMovies.size mustEqual (10)
      var year = 2012
      for (i <- 0 to 9) {
        recentMovies(i).year mustEqual (year)
        year = year - 1
      }
    }

    "allow searching by author names" in {
      movieColl.drop()
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2000, folder = 1, dvd = 2, actors = Option("Tom Cruise")))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2000, folder = 1, dvd = 2, actors = Option("Nicole Kitman")))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2000, folder = 1, dvd = 2, actors = Option("Morgan Freeman")))
      Movie2Dao.add(Movie2(title = "Dreamland", year = 2001, folder = 1, dvd = 2, actors = Option("Tom Cruise")))

      val resultsTomCruise = Movie2Dao.findByActor("Tom Cruise")
      resultsTomCruise.length mustEqual (2)

      val resultsMorganFreeman = Movie2Dao.findByActor("Morgan Freeman")
      resultsMorganFreeman.length mustBe (1)
      resultsMorganFreeman.head.actors.get mustBe ("Morgan Freeman")

      val resultsBudSpencer = Movie2Dao.findByActor("Bud Spencer")
      resultsBudSpencer.isEmpty == true
    }
  }

  "group by year" in {
    movieColl.drop()
    Movie2Dao.add(Movie2(title = "Dreamland", year = 2000, folder = 1, dvd = 2, actors = Option("Tom Cruise")))
    Movie2Dao.add(Movie2(title = "Dreamland", year = 2010, folder = 1, dvd = 2, actors = Option("Nicole Kitman")))
    Movie2Dao.add(Movie2(title = "Dreamland", year = 2010, folder = 1, dvd = 2, actors = Option("Nicole Kitman11")))
    Movie2Dao.add(Movie2(title = "Dreamland", year = 2010, folder = 1, dvd = 2, actors = Option("Nicole Kitman12")))
    Movie2Dao.add(Movie2(title = "Dreamland", year = 2000, folder = 1, dvd = 2, actors = Option("Morgan Freeman")))
    Movie2Dao.add(Movie2(title = "Dreamland", year = 2001, folder = 1, dvd = 2, actors = Option("Tom Cruise")))
    
    val groupBy = Movie2Dao.groupByYear
    
    groupBy.length must be (3)
    for ((year, count) <- groupBy) {
      year match {
        case a if a == 2000 => count must be (2)
        case b if b == 2001 => count must be (1) 
        case c if c == 2010 => count must be (3)
      }
    }
  }
}