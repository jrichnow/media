package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class AudioBook(
  val id: Option[String] = None,
  val title: String,
  val author: String,
  val plot: Option[String] = None,
  val year: Int,
  val language: Option[String] = None,
  val runtime: Option[String] = None,
  val format: Option[String] = None,
  val imageUrl: Option[String] = None,
  val genre: Option[Array[String]] = None,
  val folder: Int,
  val dvd: Int)

object AudioBook {
  
  implicit val audioBookReads: Reads[AudioBook] = (
      (__ \ "id").readNullable[String] and
      (__ \ "title").read[String] and
      (__ \ "author").read[String] and
      (__ \ "plot").readNullable[String] and
      (__ \ "year").read[Int](min(1950) keepAnd max(2030)) and
      (__ \ "language").readNullable[String] and
      (__ \ "runtime").readNullable[String](maxLength(5)) and
      (__ \ "format").readNullable[String](maxLength(3)) and
      (__ \ "imageUrl").readNullable[String] and
      (__ \ "genre").readNullable[Array[String]] and
      (__ \ "folder").read[Int](min(1) keepAnd max(10)) and
      (__ \ "dvd").read[Int](min(1) keepAnd max(200)))(AudioBook.apply _)


  implicit val audioJsonWrites = new Writes[AudioBook] {
    def writes(audio: AudioBook) = Json.obj(
      "id" -> audio.id,
      "title" -> audio.title,
      "author" -> audio.author,
      "plot" -> audio.plot,
      "year" -> audio.year,
      "language" -> audio.language,
      "runtime" -> audio.runtime,
      "format" -> audio.format,
      "imageUrl" -> audio.imageUrl,
      "genre" -> audio.genre,
      "folder" -> audio.folder,
      "dvd" -> audio.dvd)
  }

  def toJson(audioBook: AudioBook): JsValue = {
    Json.toJson(audioBook)
  }
}