package controllers

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

@RunWith(classOf[JUnitRunner])
class AudioSpec extends Specification {

  "Audio" should {

    "return the correct audio book path when adding a valid Audio book in the request" in new WithApplication {
      val result = controllers.Audio.list()(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(JsArray())
      
      val Some(resultAdd) = route(FakeRequest(POST, "/audio/add").withJsonBody(getValidAudioJson))
      
      status(resultAdd) must equalTo(OK)
      contentAsString(resultAdd) must equalTo("/audio")
      
      val resultList = controllers.Audio.list()(FakeRequest())

      status(resultList) must equalTo(OK)
      contentType(resultList) must beSome("application/json")
      contentAsJson(resultList) must equalTo(JsArray().append(getValidAudioJson))
    }
  }
  
  private def getValidAudioJson(): JsValue = {
    Json.obj(
        "title" -> "Dreamland",
        "author" -> "David K. Randall")
  }
}