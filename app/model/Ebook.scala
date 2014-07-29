package model

import anorm._
import anorm.{ Row, SQL }
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.Play.current

case class Ebook(
	id: Long,
	title: String) 
	
object Ebook {
  
  val simple = {
    get[Long]("id") ~
    get[String]("title") map {
      case id ~ title => Ebook(id, title)
    }
  }
  
  def findById(id:Long): Option[Ebook] = DB.withConnection {implicit connection => 
    SQL("select id, title from books where id={id}").on('id -> id).as(simple.singleOpt) }
}