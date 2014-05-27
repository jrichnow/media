package model

case class AudioBook(
  val title: String,
  val author: String,
  val plot: Option[String],
  val year: Int,
  val runtime: Option[String],
  val format: Option[String],
  val imageUrl: Option[String],
  val genre: Option[String],
  val folder: Int,
  val dvd: Int)