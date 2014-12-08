package utils

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite

class TheMovieDbWrapperSpec extends PlaySpec with OneAppPerSuite {

  "TheMovieDbWrapper" should {
    
    "provide the image URL based on IMDB movie ID" in {
      val configJson = TheMovieDbWrapper.getThumbnailMoviePosterUrl("")
      
      
    }
  }
}