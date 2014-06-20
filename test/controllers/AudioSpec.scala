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

class AudioSpec extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTest"

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val audioColl = db("audio")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "Audio" should {

    "return the correct audio book path when adding a valid Audio book in the request" in {
      audioColl.drop()
      val result = controllers.AudioBooks.list()(FakeRequest())

      status(result) must equal(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must equal(JsArray())

      val Some(resultAdd) = route(FakeRequest(POST, "/audio/add").withJsonBody(getValidAudioJson))
      status(resultAdd) must equal(OK)
      contentType(resultAdd) mustBe Some("application/json")
      contentAsJson(resultAdd) must equal(successValidationResponse)

      val resultList = controllers.AudioBooks.list()(FakeRequest())

      status(resultList) must equal(OK)
      contentType(resultList) mustBe Some("application/json")
      
      val bookList = contentAsJson(resultList).as[List[AudioBook]]
      
      val returnedBook = bookList.head
      val originalBook = getValidAudioJson.as[AudioBook]
      
      returnedBook.title must equal(originalBook.title)
      returnedBook.author must equal(originalBook.author)
      returnedBook.year must equal(originalBook.year)
      returnedBook.plot must equal(originalBook.plot)
      returnedBook.format must equal(originalBook.format)
      returnedBook.imageUrl must equal(originalBook.imageUrl)
      returnedBook.folder must equal(originalBook.folder)
      returnedBook.dvd must equal(originalBook.dvd)
      returnedBook.language must equal(originalBook.language)
      returnedBook.runtime must equal(originalBook.runtime)
    }
  }

  private def successValidationResponse(): JsValue = {
    Json.obj("validation" -> true, "redirectPath" -> "/audio")
  }

  private def getValidAudioJson(): JsValue = {
    Json.obj(
      "id" -> JsNull,
      "title" -> "Dreamland",
      "author" -> "David K. Randall",
      "plot" -> "some description",
      "year" -> 2020,
      "language" -> JsNull,
      "runtime" -> "13:55",
      "format" -> "mp3",
      "imageUrl" -> "http://image.url.com/path?foo=bar",
      "genre" -> Json.arr("History", "Documentary"),
      "folder" -> 1,
      "dvd" -> 12)
  }
}