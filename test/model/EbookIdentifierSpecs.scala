package model

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite

class EbookIdentifierSpecs extends PlaySpec with OneAppPerSuite {

  "EbookIdentifiers" should {

    "return a set of identifiers if they exist" in {
      val identifiers = EbookIdentifiers.findAll(341)
      
      identifiers.size must equal(3)
    }

    "return an empty set of identifiers if they don't exist" in {
      val identifiers = EbookIdentifiers.findAll(0)
      
      identifiers.size must equal(0)
    }
  }
}