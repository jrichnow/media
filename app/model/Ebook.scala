package model

import play.api.libs.json._

case class Ebook(
  id: Long,
  title: String,
  author: String,
  publicationDate: String
)

object Ebook {
  
  implicit val ebookJsonWrites = new Writes[Ebook] {
    def writes(ebook: Ebook) = Json.obj(
      "id" -> ebook.id,
      "title" -> ebook.title,
      "author" -> ebook.author,
      "publicationDate" -> checkPublicationDate(ebook.publicationDate))
  }
  
  private def checkPublicationDate(pubDate: String): String = {
    pubDate match {
      case "01/01/0101" => ""
      case _ => pubDate
    }
  }
  
  def toJson(ebook: Ebook): JsValue = {
    Json.toJson(ebook)
  }
}