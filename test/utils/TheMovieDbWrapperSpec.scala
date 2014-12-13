package utils

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import org.scalatest.Matchers

class TheMovieDbWrapperSpec extends PlaySpec {

  "TheMovieDbWrapper" should {

    "provide an actor JSON if found" in {
      TheMovieDbWrapper.init
      val actorJson = TheMovieDbWrapper.getActorData("Brad Pitt")

      JsonUtil.getIntValue(actorJson.get, "movieDbId").get mustBe (287)
      JsonUtil.getStringValue(actorJson.get, "name").get mustBe ("Brad Pitt")
      JsonUtil.getStringValue(actorJson.get, "birthDay").get mustBe ("1963-12-18")
      JsonUtil.getStringValue(actorJson.get, "birthPlace").get mustBe ("Shawnee - Oklahoma - USA")
      JsonUtil.getStringValue(actorJson.get, "deathDay").get mustBe ("")
      JsonUtil.getStringValue(actorJson.get, "biography").get.length must be > 1
      JsonUtil.getStringValue(actorJson.get, "imdbUrl").get mustBe ("http://www.imdb.com/name/nm0000093")
      JsonUtil.getStringValue(actorJson.get, "posterUrl").get mustBe ("http://image.tmdb.org/t/p/w342/2xrLcP4YRakx8aAc2jdwRbctr0Y.jpg")
    }

    "provide None if not found" in {
      TheMovieDbWrapper.init
      val actorJson = TheMovieDbWrapper.getActorData("Not Possible")
      
      actorJson mustBe (None)
    }
  }
}