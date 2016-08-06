package dao

import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import model.{Ebook, EbookDetails, EbookIdentifiers}
import play.api.db._

@Singleton
class EbookDao @Inject()(db: Database) {

  val ebookParser = {
    get[Long]("id") ~
      get[String]("title") ~
      get[String]("author_sort") ~
      get[String]("pub_date") map {
      case id ~ title ~ author ~ publicationDate => Ebook(id, title, author, publicationDate)
    }
  }

  val ebookDetailsParser = {
    get[Long]("id") ~
      get[String]("title") ~
      get[String]("author_sort") ~
      get[String]("pub_date") ~
      get[Option[String]]("text") map {
      case id ~ title ~ author ~ publicationDate ~ plot => EbookDetails(id, title, author, publicationDate, plot)
    }
  }

  val ebookIdentifierParser = {
    get[String]("type") ~
      get[String]("val") map {
      case name ~ value => EbookIdentifiers(name, value)
    }
  }

  def ebooks(): Seq[Ebook] = {
    db.withConnection { implicit conn =>
      SQL("""select id, title, author_sort, strftime('%d/%m/%Y', date(pubdate)) as pub_date from books order by title""").as {
        ebookParser.*
      }
    }
  }

  def ebookById(id: Long): Option[Ebook] = {
    db.withConnection { implicit conn =>
      SQL("""select id, title, author_sort, strftime('%d/%m/%Y', date(pubdate)) as pub_date from books where id={id}""").on('id -> id).as(ebookParser.singleOpt)
    }
  }

  def ebookDetailsById(id: Long): Option[EbookDetails] = {
    db.withConnection { implicit conn =>
      SQL(
        """select b.id, title, author_sort, strftime('%d/%m/%Y', date(pubdate)) as pub_date, text from books b
        left outer join comments c on b.id = c.book where b.id={id}""").on('id -> id).as(ebookDetailsParser.singleOpt)
    }
  }

  def identifiers(sqliteBookId: Long): Seq[EbookIdentifiers] = {
    db.withConnection { implicit conn =>
      SQL("select type, val from identifiers where book={id}").on('id -> sqliteBookId).as(ebookIdentifierParser.*)
    }
  }
}