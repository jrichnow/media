package mongodb

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication
import dao.MovieDao
import model.Movie

class UpdateMovieLocationSpec extends PlaySpec with OneAppPerSuite  {

  implicit override lazy val app: FakeApplication = FakeApplication()
  
  "MovieDao" should {
    
    "be used to update movie locations" in {
      val movies = MovieDao.findAll
      println(movies.size)
      
      val folderMovies = movies.filter(filterFolderAndDvd(_))
      println(s"number of filtered movies: ${folderMovies.size}")
      
      val updatedMovies = folderMovies.map(movie => movie.copy(hd = Some(1)))
      println(s"number of updated movies: ${updatedMovies.size}")
      
      updatedMovies.foreach(MovieDao.update(_))
    }
  }
  
  def filterFolderAndDvd(movie: Movie): Boolean = {
    movie.folder match {
      case Some(folder) if folder == 1 => {
        movie.dvd match {
          case Some(dvd) if dvd >= 147 && dvd <= 215 => true
          case Some(_) => false
          case None => false
        }
      }
      case Some(_) => false
      case None => false
    }
  }
}