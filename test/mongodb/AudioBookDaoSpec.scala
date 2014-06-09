package mongodb

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication
import com.mongodb.casbah.MongoClient
import model.AudioBook
import dao.AudioBookDao
import org.specs2.specification.BeforeExample

@RunWith(classOf[JUnitRunner])
class AudioBookDaoSpec extends Specification {
  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val audioColl = db("audio")

  "AudioBookDao" should {

    "return no audio book when collection is empty" in new WithApplication {
      audioColl.drop();
      AudioBookDao.findAll.size === 0;

      val bookOption = AudioBookDao.findById("5393ed7dc0260baea0990019")
      bookOption must beNone
    }

    "add an audio book" in new WithApplication {
      audioColl.drop();
      val audioBook = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
      audioBook.id must beNone

      val updatedAudioBook = AudioBookDao.add(audioBook)

      // Check that we have a new MongoDb id.
      updatedAudioBook.id.get.length() must be greaterThan (6)
      updatedAudioBook.year must equalTo(2012)

      val dbAudioBookOption = AudioBookDao.findById(updatedAudioBook.id.get)
      val dbAudioBook = dbAudioBookOption.get
      dbAudioBook.year must equalTo(2012)

      dbAudioBook.id.get === updatedAudioBook.id.get

      AudioBookDao.findAll.size === 1
    }

    "update an audio book" in new WithApplication {
      audioColl.drop();
      val audioBook = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
      val updatedAudioBook = AudioBookDao.add(audioBook)
      updatedAudioBook.year must equalTo(2012)

      // update.
      val modifiedAudioBook = updatedAudioBook.copy(year = 2000)
      AudioBookDao.update(modifiedAudioBook)

      val modifiedDbBook = AudioBookDao.findById(updatedAudioBook.id.get).get

      modifiedAudioBook.year must equalTo(2000)
      modifiedAudioBook.id === updatedAudioBook.id

      AudioBookDao.findAll.size === 1
    }

    "delete an audio book" in new WithApplication {
      audioColl.drop();
      val audioBook = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
      val updatedAudioBook = AudioBookDao.add(audioBook)
      
      AudioBookDao.findAll.size === 1

      AudioBookDao.delete(updatedAudioBook.id.get)
      
      AudioBookDao.findAll.size === 0
    }
  }
}