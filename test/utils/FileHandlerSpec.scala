package utils

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication
import com.mongodb.casbah.MongoClient
import dao.AudioBookDao

class FileHandlerSpec extends PlaySpec with OneAppPerSuite {
  
  val testDbName = "mediaTest"
  
  val client = MongoClient("localhost", 27017)
  val db = client(testDbName)
  val audioColl = db("audio")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "FileHandler" should {

    "export and import all audio data via a Json file" in {
      val fileName = FileHandler.exportAudio
      
      audioColl.drop()
      AudioBookDao.findAll.size === 0;
      
      FileHandler.importAudio(fileName)
    }
  }
}