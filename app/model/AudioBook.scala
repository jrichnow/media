package model

import play.api.libs.json._

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

  implicit val audioJsonWrites = new Writes[AudioBook] {
    def writes(audio: AudioBook) = Json.obj(
      "id" -> audio.id,
      "title" -> audio.title,
      "author" -> audio.author,
      "plot" -> audio.plot,
      "year" -> audio.year,
      "language" -> audio.language,
      "runtime" -> audio.runtime,
      "imageUrl" -> audio.imageUrl,
      "genre" -> audio.genre,
      "folder" -> audio.folder,
      "dvd" -> audio.dvd)
  }

  def toJson(audioBook: AudioBook): JsValue = {
    Json.toJson(audioBook)
  }
}