package sqlite

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import model.Ebook

class EbooksSpec extends PlaySpec with OneAppPerSuite {

  "Ebook" should {
    
    "should return a book based on an id" in {
    	val ebook = Ebook.findById(19).get
    	println(ebook)
    }
  }
}