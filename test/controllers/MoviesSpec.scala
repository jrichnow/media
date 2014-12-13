package controllers

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import play.api.libs.json.JsArray
import play.api.libs.json.JsNull
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.GET
import play.api.test.Helpers.OK
import play.api.test.Helpers.POST
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.contentAsString
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

  "Movies Controller (Actor)" should {

    "searching for a known actor should return the name" in {
      movieColl.drop()
      val Some(resultAdd) = route(FakeRequest(POST, "/movies/add").withJsonBody(getValidMovieJson))
      status(resultAdd) must equal(OK)

      val result = controllers.Movies.find(FakeRequest(GET, "/movies/find?entity=Actor&name=Tom+Cruise"))
      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      val movieList = contentAsJson(result).as[List[Movie]]

      val returnedMovie = movieList.head
      val originalMovie = getValidMovieJson.as[Movie]
      originalMovie.actors.get must equal("Tom Cruise")
    }

    "searching with unknown type should result in a BadRequest" in {
      val result = controllers.Movies.find(FakeRequest(GET, "/movies/find?entity=year"))
      status(result) must equal(BAD_REQUEST)
      contentAsString(result) mustBe "Search action not allowed!"
    }
  }

  "Movies Controller (Director)" should {

    "searching for a known director should return the name" in {
      movieColl.drop()
      val Some(resultAdd) = route(FakeRequest(POST, "/movies/add").withJsonBody(getValidMovieJson))
      status(resultAdd) must equal(OK)

      val result = controllers.Movies.find(FakeRequest(GET, "/movies/find?entity=Director&name=Frank+Darabont"))
      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      val movieList = contentAsJson(result).as[List[Movie]]

      val returnedMovie = movieList.head
      val originalMovie = getValidMovieJson.as[Movie]
      originalMovie.director.get must equal("Frank Darabont")
    }
  }

  "Movies Controller (Writer)" should {

    "searching for a known writershould return the name" in {
      movieColl.drop()
      val Some(resultAdd) = route(FakeRequest(POST, "/movies/add").withJsonBody(getValidMovieJson))
      status(resultAdd) must equal(OK)

      val result = controllers.Movies.find(FakeRequest(GET, "/movies/find?entity=Writer&name=Dean+Koontz"))
      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      val movieList = contentAsJson(result).as[List[Movie]]

      val returnedMovie = movieList.head
      val originalMovie = getValidMovieJson.as[Movie]
      originalMovie.writer.get must equal("Dean Koontz")
    }
  }

  private def successValidationResponse(): JsValue = {
    Json.obj("validation" -> true, "redirectPath" -> "/movies")
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
      "actors" -> "Tom Cruise",
      "director" -> "Frank Darabont",
      "writer" -> "Dean Koontz",
      "url" -> "http://imdb.com/path?foo=bar",
      "year" -> 2000,
      "folder" -> 1,
      "dvd" -> 124)
  }
}