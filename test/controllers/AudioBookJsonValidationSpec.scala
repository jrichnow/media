package controllers

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

@RunWith(classOf[JUnitRunner])
class AudioBookJsonValidationSpec extends Specification {

  "Audio" should {

    "return a correct error when missing title in audio book Json " in new WithApplication {
      val audioJson = Json.obj("author" -> "David K. Randall")
      val Some(resultAdd) = route(FakeRequest(POST, "/audio/add").withJsonBody(audioJson))
      
      status(resultAdd) must equalTo(OK)
      contentAsString(resultAdd) must equalTo("Title is missing")
    }
  }
  
  private def getValidAudioJson(): JsValue = {
    Json.obj(
        "title" -> "Dreamland",
        "author" -> "David K. Randall")
  }
}