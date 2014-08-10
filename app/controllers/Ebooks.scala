package controllers

import play.api._
import play.api.mvc._
import model.Ebook
import play.api.libs.json.Json

object Ebooks extends Controller {
  
  var ebooks: Seq[Ebook] = Seq.empty
  
  def init() {
    ebooks = Ebook.findAll
  }

  def index = Action {
    Ok(views.html.books.index())
  }
  
  def list = Action {
    if (ebooks.isEmpty) {
      ebooks = Ebook.findAll
    }
    Ok(Json.toJson(ebooks))
  }
}