package model

import anorm._
import anorm.{ Row, SQL }
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.Play.current

case class EbookIdentifiers(name: String, value: String)

object EbookIdentifiers {

  val simple = {
    get[String]("name") ~
      get[String]("val") map {
        case name ~ value => EbookIdentifiers(name, value)
      }
  }
  
  def findAll(sqliteBookId: Long):Seq[EbookIdentifiers] = DB.withConnection { implicit connection =>
    val identifiers = SQL("select type, val from identifiers where book={id}").on('id -> sqliteBookId)
    identifiers().map(row => EbookIdentifiers(row[String]("type"), row[String]("val"))).toList
  }
}