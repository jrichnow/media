package controllers

import javax.inject.{Inject, Singleton}

import dao.EbookDao
import play.api.mvc._
import model.Ebook
import play.api.libs.json.Json
import model.EbookDetails
import play.api.libs.json.JsValue
import utils.BookDetailsCollator

@Singleton
class Ebooks @Inject()(dao: EbookDao, collator: BookDetailsCollator) extends Controller {
  
  var ebooks: Seq[Ebook] = dao.ebooks()
  
  def index = Action {
    Ok(views.html.books.index())
  }
  
  def list = Action {
    if (ebooks.isEmpty) {
      ebooks = dao.ebooks()
    }
    Ok(Json.toJson(ebooks))
  }
  
  def detailsForm(id: Int) = Action {
    Ok(views.html.books.details(id))
  }
  
  def details(id: Int) = Action {
    Ok(collator.getBookDetails(id))
  }
  
  private def detailsJson(details: EbookDetails): JsValue = {
    Json.obj("id" -> details.id,
      "title" -> details.title,
      "author" -> details.author,
      "publicationDate" -> checkPublicationDate(details.publicationDate))
  }
  
  private def checkPublicationDate(pubDate: String): String = {
    pubDate match {
      case "01/01/0101" => ""
      case _ => pubDate
    }
  }
}