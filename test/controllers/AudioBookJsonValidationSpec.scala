package controllers

import scala.math.BigDecimal.int2bigDecimal
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.__
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import play.api.test.Helpers.POST
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.contentType
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsJson
import play.api.test.WithApplication
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication

class AudioBookJsonValidationSpec extends PlaySpec with OneAppPerSuite {

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> "mediaTest"))
    
  "Audio" should {

    "return a redirect URL when audio book with all fields is valid" in {
      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(getValidAudioJsonFull))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (successValidationResponse)
    }

    "return a redirect URL when audio book with all required fields is valid" in {
      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(getValidAudioJsonRequiredOnly))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (successValidationResponse)
    }

    "return a correct error list when missing title in audio book Json " in {
      val jsonTransformer = (__ \ 'title).json.prune
      val missingTitle = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingTitle))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("title", "error.path.missing"))
    }

    "return a correct error list entry when missing author in audio book Json " in {
      val jsonTransformer = (__ \ 'author).json.prune
      val missingAuthor = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingAuthor))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("author", "error.path.missing"))
    }

    "return a correct error list entry when missing year in audio book Json" in {
      val jsonTransformer = (__ \ 'year).json.prune
      val missingYear = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingYear))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("year", "error.path.missing"))
    }

    "return a correct error list entry when year is a string in audio book Json" in {
      val wrongYear = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("year" -> JsString("2014"))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongYear))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("year", "error.expected.jsnumber"))
    }

    "return a correct error list entry when year is too early in audio book Json" in {
      val wrongYear = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("year" -> JsNumber(1949))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongYear))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("year", "error.min"))
    }

    "return a correct error list entry when year is too big in audio book Json" in {
      val wrongYear = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("year" -> JsNumber(2031))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongYear))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("year", "error.max"))
    }

    "return a correct error list entry when runtime is longer than 5 characters in audio book Json" in {
      val wrongRuntime = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("runtime" -> JsString("12:231"))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongRuntime))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("runtime", "error.maxLength"))
    }

    "return a correct error list entry when format is longer than 3 characters in audio book Json" in {
      val wrongFormat = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("format" -> JsString("mp34"))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongFormat))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("format", "error.maxLength"))
    }

    "return a correct error list entry when missing folder in audio book Json" in {
      val jsonTransformer = (__ \ 'folder).json.prune
      val missingFolder = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingFolder))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("folder", "error.path.missing"))
    }

    "return a correct error list entry when folder is less than one in audio book Json" in {
      val wrongFolder = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("folder" -> JsNumber(0))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongFolder))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("folder", "error.min"))
    }

    "return a correct error list entry when folder is more than ten in audio book Json" in {
      val wrongFolder = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("folder" -> JsNumber(11))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongFolder))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("folder", "error.max"))
    }

    "return a correct error list entry when missing dvd in audio book Json " in {
      val jsonTransformer = (__ \ 'dvd).json.prune
      val missingDvd = getValidAudioJsonRequiredOnly.transform(jsonTransformer).get

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(missingDvd))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("dvd", "error.path.missing"))
    }

    "return a correct error list entry when dvd is less than one in audio book Json" in {
      val wrongDvd = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("dvd" -> JsNumber(0))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongDvd))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("dvd", "error.min"))
    }

    "return a correct error list entry when dvd is more than 200 in audio book Json" in {
      val wrongDvd = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("dvd" -> JsNumber(201))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongDvd))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("dvd", "error.max"))
    }

    "return a correct error list entry when genre is not an array in audio book Json" in {
      val wrongGenre = getValidAudioJsonRequiredOnly.as[JsObject] ++ Json.obj("genre" -> JsString("History"))

      val Some(result) = route(FakeRequest(POST, "/audio/add").withJsonBody(wrongGenre))

      status(result) must equal (OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal (failureValidationResponse("genre", "error.expected.jsarray"))
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
      "genre" -> Json.arr("History", "Documentary"),
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