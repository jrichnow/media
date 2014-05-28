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

    "return a redirect URL when audio book with all fields is valid" in new WithApplication {
      val audioJson = Json.obj("author" -> "David K. Randall")
      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(getValidAudioJsonFull))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(successValidationResponse)
    }

    "return a redirect URL when audio book with all required fields is valid" in new WithApplication {
      val audioJson = Json.obj("author" -> "David K. Randall")
      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(getValidAudioJsonRequiredOnly))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(successValidationResponse)
    }

    "return a correct error list when missing title in audio book Json " in new WithApplication {
      val jsonTransformer = (__ \ 'title).json.prune
      val missingTitle = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingTitle))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("title", "error.path.missing"))
    }

    "return a correct error list entry when missing author in audio book Json " in new WithApplication {
      val jsonTransformer = (__ \ 'author).json.prune
      val missingAuthor = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingAuthor))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("author", "error.path.missing"))
    }

    "return a correct error list entry when missing year in audio book Json" in new WithApplication {
      val jsonTransformer = (__ \ 'year).json.prune
      val missingYear = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingYear))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("year", "error.path.missing"))
    }

    "return a correct error list entry when year is a string in audio book Json" in new WithApplication {
      val wrongYear = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("year" -> JsString("2014"))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongYear))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("year", "error.expected.jsnumber"))
    }

    "return a correct error list entry when year is too early in audio book Json" in new WithApplication {
      val wrongYear = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("year" -> JsNumber(1949))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongYear))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("year", "error.expected.jsnumber"))
    }

    "return a correct error list entry when missing folder in audio book Json" in new WithApplication {
      val jsonTransformer = (__ \ 'folder).json.prune
      val missingFolder = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingFolder))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("folder", "error.path.missing"))
    }

    "return a correct error list entry when missing dvd in audio book Json " in new WithApplication {
      val jsonTransformer = (__ \ 'dvd).json.prune
      val missingDvd = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingDvd))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("dvd", "error.path.missing"))
    }
  }

  private def successValidationResponse(): JsValue = {
    Json.obj("validation" -> true, "redirectPath" -> "/audio")
  }

  private def failureValidationResponse(path: String, errorMessage: String): JsValue = {
    Json.obj("validation" -> false, "errorList" -> JsArray(Seq(Json.obj(path -> errorMessage))))
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

  private def getValidAudioJsonRequiredOnly(): JsValue = {
    Json.obj(
      "title" -> "Dreamland",
      "author" -> "David K. Randall",
      "year" -> 2020,
      "folder" -> 1,
      "dvd" -> 12)
  }
}