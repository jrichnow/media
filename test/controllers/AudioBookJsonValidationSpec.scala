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
      contentAsJson(result) must equalTo(failureValidationResponse("year", "error.min"))
    }

    "return a correct error list entry when year is too big in audio book Json" in new WithApplication {
      val wrongYear = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("year" -> JsNumber(2031))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongYear))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("year", "error.max"))
    }

    "return a correct error list entry when runtime is longer than 5 characters in audio book Json" in new WithApplication {
      val wrongRuntime = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("runtime" -> JsString("12:231"))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongRuntime))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("runtime", "error.maxLength"))
    }

    "return a correct error list entry when format is longer than 3 characters in audio book Json" in new WithApplication {
      val wrongFormat = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("format" -> JsString("mp34"))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongFormat))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("format", "error.maxLength"))
    }

    "return a correct error list entry when missing folder in audio book Json" in new WithApplication {
      val jsonTransformer = (__ \ 'folder).json.prune
      val missingFolder = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingFolder))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("folder", "error.path.missing"))
    }

    "return a correct error list entry when folder is less than one in audio book Json" in new WithApplication {
      val wrongFolder = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("folder" -> JsNumber(0))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongFolder))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("folder", "error.min"))
    }

    "return a correct error list entry when folder is more than ten in audio book Json" in new WithApplication {
      val wrongFolder = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("folder" -> JsNumber(11))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongFolder))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("folder", "error.max"))
    }

    "return a correct error list entry when missing dvd in audio book Json " in new WithApplication {
      val jsonTransformer = (__ \ 'dvd).json.prune
      val missingDvd = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingDvd))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("dvd", "error.path.missing"))
    }

    "return a correct error list entry when dvd is less than one in audio book Json" in new WithApplication {
      val wrongDvd = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("dvd" -> JsNumber(0))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongDvd))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("dvd", "error.min"))
    }

    "return a correct error list entry when dvd is more than 200 in audio book Json" in new WithApplication {
      val wrongDvd = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("dvd" -> JsNumber(201))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongDvd))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result) must equalTo(failureValidationResponse("dvd", "error.max"))
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