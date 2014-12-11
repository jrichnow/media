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

      JsonUtil.getIntValue(actorJson, "id").get mustBe (287)
      JsonUtil.getStringValue(actorJson, "name").get mustBe ("Brad Pitt")
      JsonUtil.getStringValue(actorJson, "birthday").get mustBe ("1963-12-18")
      JsonUtil.getStringValue(actorJson, "birthplace").get mustBe ("Shawnee - Oklahoma - USA")
      JsonUtil.getStringValue(actorJson, "deathday").get mustBe ("")
      JsonUtil.getStringValue(actorJson, "biography").get.length must be >1
      JsonUtil.getStringValue(actorJson, "imdbUrl").get mustBe ("http://www.imdb.com/name/nm0000093")
      JsonUtil.getStringValue(actorJson, "poster").get mustBe ("http://image.tmdb.org/t/p/w342/2xrLcP4YRakx8aAc2jdwRbctr0Y.jpg")
    }

    def expectedJson(): JsValue = {
      Json.obj()
    }
  }
}