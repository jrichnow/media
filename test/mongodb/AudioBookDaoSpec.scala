package mongodb

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication
import com.mongodb.casbah.MongoClient
import model.AudioBook
import dao.AudioBookDao

@RunWith(classOf[JUnitRunner])
class AudioBookDaoSpec extends Specification {
  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val audioColl = db("audio")

  "AudioBookDao" should {

    "crud" in new WithApplication {
      // insert.
      val audioBook = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
      val updatedAudioBook = AudioBookDao.add(audioBook)
      println(updatedAudioBook.id)
      
      updatedAudioBook.id.get.length() must be greaterThan(6)
      updatedAudioBook.year must equalTo(2012)
      
      // update.
      val modifiedAudioBook = updatedAudioBook.copy(year=2000)
      AudioBookDao.update(modifiedAudioBook)
      
      val modifiedDbBook = AudioBookDao.findById(updatedAudioBook.id.get).get
      
      modifiedAudioBook.year must equalTo(2000)
      modifiedAudioBook.id must equalTo(updatedAudioBook.id)

      // delete.
    }
  }
}