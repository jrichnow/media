package model

import play.api.libs.json._

case class AudioBook(
  val title: String,
  val author: String,
  val plot: Option[String],
  val year: Int,
  val language: Option[String],
  val runtime: Option[String],
  val format: Option[String],
  val imageUrl: Option[String],
  val genre: Option[Array[String]],
  val folder: Int,
  val dvd: Int)
  
object AudioBook {
  
  implicit val audioJsonWrites = new Writes[AudioBook] {
    def writes(audio: AudioBook) = Json.obj(
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