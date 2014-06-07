package mongodb

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication
import com.mongodb.casbah.MongoClient
import model.AudioBook

@RunWith(classOf[JUnitRunner])
class AudioBookDaoSpec extends Specification {
  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val audioColl = db("audio")

  "AudioBookDao" should {

    "crud" in new WithApplication {
      // insert.
      val audio = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)

      // update.

      // delete.
    }
  }
}