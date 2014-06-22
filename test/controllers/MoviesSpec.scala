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
import model.AudioBook
import model.Movie

class MoviesSpec extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTest"

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val movieColl = db("movie")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "Movies Controller" should {

    "return the correct redirect path when adding a valid movie in the request" in {
      movieColl.drop()
      val result = controllers.Movies.list()(FakeRequest())
      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(JsArray())
      println("--1")

      val Some(resultAdd) = route(FakeRequest(POST, "/movies/add").withJsonBody(getValidMovieJson))
      status(resultAdd) must equal(OK)
      contentType(resultAdd) mustBe Some("application/json")
      contentAsJson(resultAdd) must equal(successValidationResponse)

      val resultList = controllers.Movies.list()(FakeRequest())

      status(resultList) must equal(OK)
      contentType(resultList) mustBe Some("application/json")
      
      val movieList = contentAsJson(resultList).as[List[Movie]]
      
      val returnedMovie = movieList.head
      val originalMovie = getValidMovieJson.as[Movie]
      
      returnedMovie.title must equal(originalMovie.title)
      returnedMovie.year must equal(originalMovie.year)
      returnedMovie.url must equal(originalMovie.url)
      returnedMovie.folder must equal(originalMovie.folder)
      returnedMovie.dvd must equal(originalMovie.dvd)
      returnedMovie.language must equal(originalMovie.language)
    }
  }

  private def successValidationResponse(): JsValue = {
    Json.obj("validation" -> true, "redirectPath" -> "/movie")
  }

  private def getValidMovieJson(): JsValue = {
    Json.obj(
      "id" -> JsNull,
      "title" -> "Matrix",
      "alternativeTitle" -> "Matrixy",
      "originalTitle" -> "Matrix!",
      "language" -> JsNull,
      "subTitle" -> JsNull,
      "genre" -> Json.arr("Action", "Drama"),
      "url" -> "http://imdb.com/path?foo=bar",
      "year" -> 2000,
      "folder" -> 1,
      "dvd" -> 124)
  }
}