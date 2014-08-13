package model

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import java.util.Date
import java.text.SimpleDateFormat

class EbookSpecs extends PlaySpec with OneAppPerSuite {

  val dateFormat = new SimpleDateFormat("dd/MM/yyyy")

  "Ebook" should {

    "express itself as valid Json" in {
      val ebook = new Ebook(341, "Envy", "Sandra Brown", "15/04/2001")
      getEbookJson(ebook) must equal(Ebook.toJson(ebook))
    }
  }

  private def getEbookJson(ebook: Ebook): JsValue = {
    Json.obj(
      "id" -> ebook.id,
      "title" -> ebook.title,
      "author" -> ebook.author,
      "publicationDate" -> ebook.publicationDate)
  }
}