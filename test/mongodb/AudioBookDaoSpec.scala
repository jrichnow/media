package mongodb

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import com.mongodb.casbah.MongoClient
import dao.AudioBookDao
import model.AudioBook
import play.api.test.FakeApplication
import play.api.test.WithApplication
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite

class AudioBookDaoSpec extends PlaySpec with OneAppPerSuite {
  
  val testDbName = "mediaTest"
  
  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val audioColl = db("audio")
  
  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "AudioBookDao" should {

    "return no audio book when collection is empty" in {
      audioColl.drop()
      AudioBookDao.findAll.size === 0

      val bookOption = AudioBookDao.findById("5393ed7dc0260baea0990019")
      bookOption mustBe None 
    }

    "add an audio book" in  {
      audioColl.drop()
      val audioBook = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
      audioBook.id mustBe None

      val updatedAudioBook = AudioBookDao.add(audioBook)

      // Check that we have a new MongoDb id.
      updatedAudioBook.id.get.length() >= 6
      updatedAudioBook.year must equal (2012)

      val dbAudioBookOption = AudioBookDao.findById(updatedAudioBook.id.get)
      val dbAudioBook = dbAudioBookOption.get
      dbAudioBook.year must equal (2012)

      dbAudioBook.id.get === updatedAudioBook.id.get

      AudioBookDao.findAll.size === 1
    }

    "update an audio book" in {
      audioColl.drop()
      val audioBook = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
      val updatedAudioBook = AudioBookDao.add(audioBook)
      updatedAudioBook.year must equal (2012)

      // update.
      val modifiedAudioBook = updatedAudioBook.copy(year = 2000)
      AudioBookDao.update(modifiedAudioBook)

      val modifiedDbBook = AudioBookDao.findById(updatedAudioBook.id.get).get

      modifiedAudioBook.year must equal (2000)
      modifiedAudioBook.id === updatedAudioBook.id

      AudioBookDao.findAll.size === 1
    }

    "delete an audio book" in {
      audioColl.drop()
      val audioBook = AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2)
      val updatedAudioBook = AudioBookDao.add(audioBook)
      
      AudioBookDao.findAll.size === 1

      AudioBookDao.delete(updatedAudioBook.id.get)
      
      AudioBookDao.findAll.size === 0
    }
    
    "return a limited list of recent audio books" in {
      audioColl.drop()
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2000, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2001, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2002, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2003, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2004, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2005, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2006, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2007, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2008, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2009, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2010, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2011, folder = 1, dvd = 2))
      AudioBookDao.add(AudioBook(title = "Dreamland", author = "Mr. Writer", year = 2012, folder = 1, dvd = 2))
      
      val recentBooks = AudioBookDao.recent
      
      recentBooks.size === 10
      var year = 2012
      for (i <- 0 to 9) {
    	  recentBooks(i).year === year
    	  year = year - 1
      }
    }
  }
}