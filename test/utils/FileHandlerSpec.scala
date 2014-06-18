package utils

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import play.api.test.WithApplication
import play.api.test.WithApplication

@RunWith(classOf[JUnitRunner])
class FileHandlerSpec extends Specification {

  "FileHandler" should {

    "export all audio data to a file in JSON format" in new WithApplication {
      FileHandler.exportAudio
    }

    "import a JSON file and insert all data into MongoDb" in new WithApplication {
      FileHandler.importAudio
    }
  }
}