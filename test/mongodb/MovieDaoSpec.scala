package mongodb

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import com.mongodb.casbah.MongoClient
import dao.Movie2Dao
import model.AudioBook
import play.api.test.FakeApplication
import play.api.test.WithApplication
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import model.Movie2
import model.Movie
import dao.MovieDao

class MovieDaoSpec extends PlaySpec with OneAppPerSuite {
  
  val testDbName = "mediaTest"
  
  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val movieColl = db("movie")
  
  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "MovieDao" should {

    "return no movie when collection is empty" in {
      movieColl.drop()
      Movie2Dao.findAll.size === 0

      val movieOption = Movie2Dao.findById("5393ed7dc0260baea0990019")
      movieOption mustBe None 
    }

    "add a movie" in  {
      movieColl.drop()
      val movie = Movie(title = "Dreamland", year = 2012, folder = 1, dvd = 2)
      movie.id mustBe None

      val updatedMovie = MovieDao.add(movie)

      // Check that we have a new MongoDb id.
      updatedMovie.id.get.length() >= 6
      updatedMovie.year must equal (2012)

      val dbMovieOption = MovieDao.findById(updatedMovie.id.get)
      val dbMovie = dbMovieOption.get
      dbMovie.year must equal (2012)

      dbMovie.id.get === updatedMovie.id.get

      MovieDao.findAll.size === 1
    }

    "update a movie" in {
      movieColl.drop()
      val movie = Movie(title = "Dreamland", year = 2012, folder = 1, dvd = 2)
      val updatedMovie = MovieDao.add(movie)
      updatedMovie.year must equal (2012)

      // update.
      val modifiedMovie = updatedMovie.copy(year = 2000)
      MovieDao.update(modifiedMovie)

      val modifiedDbMovie = MovieDao.findById(updatedMovie.id.get).get

      modifiedMovie.year must equal (2000)
      modifiedMovie.id === updatedMovie.id

      MovieDao.findAll.size === 1
    }

    "delete a movie" in {
      movieColl.drop()
      val movie = Movie(title = "Dreamland", year = 2012, folder = 1, dvd = 2)
      val updatedMovie = MovieDao.add(movie)
      
      MovieDao.findAll.size === 1

      MovieDao.delete(updatedMovie.id.get)
      
      MovieDao.findAll.size === 0
    }
    
    "return a limited list of recent movies" in {
      movieColl.drop()
      MovieDao.add(Movie(title = "Dreamland", year = 2000, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2001, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2002, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2003, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2004, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2005, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2006, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2007, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2008, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2009, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2010, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2011, folder = 1, dvd = 2))
      MovieDao.add(Movie(title = "Dreamland", year = 2012, folder = 1, dvd = 2))
      
      val recentMovies = MovieDao.recent
      
      recentMovies.size === 10
      var year = 2012
      for (i <- 0 to 9) {
    	  recentMovies(i).year === year
    	  year = year - 1
      }
    }
  }
}