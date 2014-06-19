package utils

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication


class FileHandlerSpec extends PlaySpec with OneAppPerSuite {
  
  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> "mediaTest"))

  "FileHandler" should {

    "export all audio data to a file in JSON format" in {
      FileHandler.exportAudio
    }

    "import a JSON file and insert all data into MongoDb" in {
      FileHandler.importAudio
    }
  }
}