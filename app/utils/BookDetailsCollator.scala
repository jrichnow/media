package utils

import play.api.libs.json.JsValue
import model.EbookDetails
import play.api.libs.json.Json
import dispatch._
import dispatch.Defaults._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import javax.inject.{Inject, Singleton}

import dao.EbookDao

@Singleton
class BookDetailsCollator @Inject()(dao: EbookDao) {

  def getBookDetails(id: Int): JsValue = {
    val details = dao.ebookDetailsById(id)
    val imageUrl = getImageUrl(details.get.id)

    toJson(details.get, imageUrl.getOrElse("/assets/images/no-image.jpg"))
  }

  private def toJson(ebook: EbookDetails, imageUrl: String): JsValue = {
    Json.obj(
      "id" -> ebook.id,
      "title" -> ebook.title,
      "author" -> ebook.author,
      "publicationDate" -> checkPublicationDate(ebook.publicationDate),
      "plot" -> ebook.plot,
      "imageUrl" -> imageUrl)
  }

  private def checkPublicationDate(pubDate: String): String = {
    pubDate match {
      case "01/01/0101" => ""
      case _ => pubDate
    }
  }

  private def getImageUrl(sqliteBookId: Long): Option[String] = {
    val identifiers = dao.identifiers(sqliteBookId)
    val isbnOption = identifiers.find(_.name == "isbn")
    isbnOption match {
      case Some(_) => { 
        val json = getOpenLibraryData(isbnOption.get.value)
        getImageUrlFromOpenLibraryData(json)
      }
      case None => None
    }
  }

  private def getOpenLibraryData(isbn: String): JsValue = {
    val request = url(s"http://openlibrary.org/api/books?bibkeys=ISBN:$isbn&jscmd=data&format=json")
    val response = Http(request OK as.String)
    val jsonString = Await.result(response, Duration(10, "s"))
    println(jsonString)
    Json.parse(jsonString)
  }
  
  private def getImageUrlFromOpenLibraryData(json: JsValue): Option[String] = {
    val urlList = (json \\ "large")
    if (urlList.isEmpty)
      None
    else
      urlList(0).asOpt[String]
  }
}