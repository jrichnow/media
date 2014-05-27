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
      val audioJson = Json.obj("author" -> "David K. Randall", "year" -> 2014)
      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(audioJson))

      status(result) must equalTo(OK)
//      contentType(result) must beSome("application/json")
      contentAsString(result) must equalTo("Title is missing")
    }

    "return a redirect URL when audio book Json is valid" in new WithApplication {
      val audioJson = Json.obj("author" -> "David K. Randall")
      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(getValidAudioJsonFull))

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/plain")
      contentAsString(result) must equalTo("/audio")
    }
  }

  private def getValidAudioJsonFull(): JsValue = {
    Json.obj(
      "title" -> "Dreamland",
      "author" -> "David K. Randall",
      "plot" -> "some description",
      "year" -> 2020,
      "runtime" -> "13:55",
      "format" -> "mp3",
      "imageUrl" -> "http://image.url.com/path?foo=bar",
      "genre" -> "History",
      "folder" -> 1,
      "dvd" -> 12)
  }
}