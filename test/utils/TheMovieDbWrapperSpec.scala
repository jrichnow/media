package utils

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite

class TheMovieDbWrapperSpec extends PlaySpec with OneAppPerSuite {

  "TheMovieDbWrapper" should {
    
    "provide an actor JSON if found" in {
      val actorJson = TheMovieDbWrapper.getActorData("Brad Pitt")
      
      println(actorJson)
    }
  }
}