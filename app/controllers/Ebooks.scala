package controllers

import play.api._
import play.api.mvc._
import model.Ebook

object Ebooks extends Controller {
  
  var ebooks: Seq[Ebook] = Seq.empty
  
  def init() {
    ebooks = Ebook.findAll
  }

  def index = Action {
    Ok(views.html.books.index())
  }

}