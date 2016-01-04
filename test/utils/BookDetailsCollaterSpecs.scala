package utils

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import play.api.Application
import play.api.test.FakeApplication
import play.api.GlobalSettings
import play.api.test._
import play.api.test.Helpers._


class BookDetailsCollaterSpecs extends FlatSpec with BeforeAndAfterAll {

  override def beforeAll() {
    setupData()
  }

  def setupData() {
    running(FakeApplication(withGlobal = Some(new GlobalSettings() {
      override def onStart(app: Application) { println("Ignoring Global startup") }
    }))) {
    }
  }

  "Collator" should "" in {
    running(FakeApplication()) {
      val details = BookDetailsCollator.getBookDetails(341)
      println(details)
    }
  }
}