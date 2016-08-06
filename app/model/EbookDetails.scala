package model

import dispatch._
import dispatch.Defaults._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.Writes

case class EbookDetails(
  id: Long,
  title: String,
  author: String,
  publicationDate: String,
  plot: Option[String])

case class EbookIdentifiers(name: String, value: String)

object EbookDetails {

  private def getOpenLibraryData(isbn: String) {
    val request = url(s"http://openlibrary.org/api/books?bibkeys=ISBN:$isbn&jscmd=data&format=json")
    val response = Http(request OK as.String)
    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
  }

  implicit val ebookJsonWrites = new Writes[EbookDetails] {
    def writes(ebook: EbookDetails) = Json.obj(
      "id" -> ebook.id,
      "title" -> ebook.title,
      "author" -> ebook.author,
      "publicationDate" -> checkPublicationDate(ebook.publicationDate),
      "plot" -> ebook.plot)
  }

  private def checkPublicationDate(pubDate: String): String = {
    pubDate match {
      case "01/01/0101" => ""
      case _ => pubDate
    }
  }

  def toJson(ebook: EbookDetails): JsValue = {
    Json.toJson(ebook)
  }
}