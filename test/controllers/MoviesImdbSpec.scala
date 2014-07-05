package controllers

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import play.api.libs.json.JsArray
import play.api.libs.json.JsNull
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
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
import com.mongodb.casbah.MongoClient
import model.Movie2

class MoviesImdbSpec extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTest"
  val requestPath = "/movies/addImdb";

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val movieColl = db("movie")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "Movies Controller" should {

    "return the correct error when entire JSON is missing in the request" in {
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(JsNull))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponseAllMissing())
    }

    "return the correct error when IMDB ID is missing in the request" in {
      val missingImdbId = Json.obj("dvd" -> 1, "folder" -> 1)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("imdbId", "error.path.missing"))
    }

    "return the correct error when IMDB ID is unknown in the request" in {
      val missingImdbId = Json.obj("imdbId" -> "test", "dvd" -> 1, "folder" -> 1)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(Json.obj("validation" -> false, "error" -> "Incorrect IMDb ID"))
    }

    "return the correct error when folder is missing in the request" in {
      val missingImdbId = Json.obj("imdbId" -> "tt1234567", "dvd" -> 1)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("folder", "error.path.missing"))
    }

    "return the correct error when folder < 1 in the request" in {
      val missingImdbId = Json.obj("imdbId" -> "tt1234567", "folder" -> 0, "dvd" -> 1)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("folder", "error.min"))
    }

    "return the correct error when folder > 10 in the request" in {
      val missingImdbId = Json.obj("imdbId" -> "tt1234567", "folder" -> 11, "dvd" -> 1)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("folder", "error.max"))
    }

    "return the correct error when dvd is missing in the request" in {
      val missingImdbId = Json.obj("imdbId" -> "tt1234567", "folder" -> 1)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("dvd", "error.path.missing"))
    }

    "return the correct error when dvd < 1 in the request" in {
      val missingImdbId = Json.obj("imdbId" -> "tt1234567", "folder" -> 1, "dvd" -> 0)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("dvd", "error.min"))
    }

    "return the correct error when dvd > 300 in the request" in {
      val missingImdbId = Json.obj("imdbId" -> "tt1234567", "folder" -> 1, "dvd" -> 301)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("dvd", "error.max"))
    }

    "return the correct redirect path if validation passes" in {
      val missingImdbId = Json.obj("imdbId" -> "tt1234567", "dvd" -> 1, "folder" -> 1)
      val Some(result) = route(FakeRequest(POST, requestPath).withJsonBody(missingImdbId))

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(failureValidationResponse("imdbId", "error.path.missing"))
    }
  }

  private def successValidationResponse(id: String): JsValue = {
    Json.obj("validation" -> true, "redirectPath" -> s"/movie/$id")
  }

  private def failureValidationResponse(path: String, errorMessage: String): JsValue = {
    Json.obj("validation" -> false, "errorList" -> JsArray(Seq(Json.obj(path -> errorMessage))))
  }

  private def failureValidationResponseAllMissing(): JsValue = {
    Json.obj("validation" -> false, "errorList" -> JsArray(
      Seq(Json.obj("dvd" -> "error.path.missing"),
        Json.obj("imdbId" -> "error.path.missing"),
        Json.obj("folder" -> "error.path.missing"))))
  }
}