package model

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes

case class File(
  val name: String,
  val size: String,
  val lastMod: Long)

object File {

  implicit val fileWrites = new Writes[File] {
    def writes(file: File) = Json.obj(
      "name" -> file.name,
      "size" -> file.size,
      "lastMod" -> file.lastMod)
  }

  def toJson(file: File): JsValue = {
    Json.toJson(file)
  }
}