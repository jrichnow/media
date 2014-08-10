package sqlite

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import model.EbookDetails
import model.Ebook

class EbooksSpec extends PlaySpec with OneAppPerSuite {

  "Ebook" should {

    "should return a book based on an id" in {
      val ebook = new Ebook(341, "Envy", "Brown, Sandra", "15/04/2001")
      
      val ebookDb = Ebook.findById(341).get
      ebook must equal(ebookDb)
      //    	Ebook.getOpenLibraryData(ebook.isbn.get)
    }
  }
}