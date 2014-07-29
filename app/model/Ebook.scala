package model

import anorm._
import anorm.{ Row, SQL }
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.Play.current

case class Ebook(
	id: Long,
	title: String,
	isbn: Option[String]) 
	
object Ebook {
  
  val simple = {
    get[Long]("id") ~
    get[String]("title") ~
    get[Option[String]]("val") map {
      case id ~ title ~ isbn => Ebook(id, title, isbn)
    }
  }
  
  def findById(id:Long): Option[Ebook] = DB.withConnection {implicit connection => 
    SQL("select books.id, title, type, val from books join identifiers on books.id = identifiers.book where books.id={id} and type='isbn'").on('id -> id).as(simple.singleOpt) }
}