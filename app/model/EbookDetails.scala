package model

import anorm._
import anorm.{ Row, SQL }
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.Play.current
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

object EbookDetails {

  val simple = {
    get[Long]("id") ~
      get[String]("title") ~
      get[String]("author_sort") ~
      get[String]("pub_date") ~
      get[Option[String]]("text") map {
        case id ~ title ~ author ~ publicationDate ~ plot => EbookDetails(id, title, author, publicationDate, plot)
      }
  }

  def findById(id: Long): Option[EbookDetails] = DB.withConnection { implicit connection =>
    SQL("""select b.id, title, author_sort, strftime('%d/%m/%Y', date(pubdate)) as pub_date, text from books b 
        left outer join comments c on b.id = c.book where b.id={id}""").on('id -> id).as(simple.singleOpt)
  }

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