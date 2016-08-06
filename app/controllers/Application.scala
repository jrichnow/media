package controllers

import javax.inject.Inject

import play.api.mvc._

class Application @Inject()(movies: Movies, audioBooks: AudioBooks, ebooks: Ebooks) extends Controller {

  def index = Action {
    Ok(views.html.home(movies.getSize(), audioBooks.getSize(), ebooks.ebooks.size))
  }
}