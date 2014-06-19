package utils

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import dao.AudioBookDao

class ConfigTest extends PlaySpec with OneAppPerSuite {

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> "mediaTest"))

  "The OneAppPerSuite trait" must {
    "provide a FakeApplication" in {
      app.configuration.getString("mongodb.media.db") mustBe Some("mediaTest")
      val books = AudioBookDao.findAll
      books.foreach(println(_))
    }
  }
}