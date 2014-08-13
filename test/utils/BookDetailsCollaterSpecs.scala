package utils

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite

class BookDetailsCollaterSpecs extends PlaySpec with OneAppPerSuite {

  "Collator" should {
    "" in {
      val details = BookDetailsCollator.getBookDetails(341)
      println(details)
    }
  }
}