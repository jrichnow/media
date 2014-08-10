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
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.util.Date
import java.text.SimpleDateFormat

case class Ebook(
  id: Long,
  title: String,
  author: String,
  publicationDate: String
)

object Ebook {
  
  val simple = {
    get[Long]("id") ~
      get[String]("title") ~
      get[String]("author_sort") ~
      get[String]("pub_date") map {
        case id ~ title ~ author ~ publicationDate => Ebook(id, title, author, publicationDate)
      }
  }
  
  def findAll():Seq[Ebook] = DB.withConnection { implicit connection =>
    val ebooks = SQL("""select id, title, author_sort, strftime('%d/%m/%Y', date(pubdate)) as pub_date from books""")
    ebooks().map(row => Ebook(row[Long]("id"), row[String]("title"), row[String]("author_sort"), row[String]("pub_date")))
  }

  def findById(id: Long): Option[Ebook] = DB.withConnection { implicit connection =>
    SQL("""select id, title, author_sort, strftime('%d/%m/%Y', date(pubdate)) as pub_date from books where id={id}""").on('id -> id).as(simple.singleOpt)
  }

  def getOpenLibraryData(isbn: String) {
    val request = url(s"http://openlibrary.org/api/books?bibkeys=ISBN:$isbn&jscmd=data&format=json")
    val response = Http(request OK as.String)
    val omdbJsonString = Await.result(response, Duration(10, "s"))
    println(omdbJsonString)
  }

  implicit val ebookJsonWrites = new Writes[Ebook] {
    def writes(ebook: Ebook) = Json.obj(
      "id" -> ebook.id,
      "title" -> ebook.title,
      "author" -> ebook.author,
      "publicationDate" -> ebook.publicationDate)
  }

  def toJson(ebook: Ebook): JsValue = {
    Json.toJson(ebook)
  }
}