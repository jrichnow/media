package utils

import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map

import model.Actor

object JsonUtil {

  def getJsArray(json: JsValue, arrayName: String): Option[JsArray] = {
    (json \ arrayName).validate[JsArray] match {
      case s: JsSuccess[JsArray] => Option(s.get)
      case e: JsError => None
    }
  }

  def getStringValue(json: JsValue, tagName: String): Option[String] = {
    (json \ tagName).validate[String] match {
      case s: JsSuccess[String] => Option(s.get)
      case e: JsError => None
    }
  }

  def getIntValue(json: JsValue, tagName: String): Option[Int] = {
    (json \ tagName).validate[Int] match {
      case s: JsSuccess[Int] => Option(s.get)
      case e: JsError => None
    }
  }

  def searchResultByNameToJson(map: Map[Actor, Seq[(String, Int)]]): JsValue = {

    def getEntityCountAsJson(details: Seq[(String, Int)]): JsArray = {
      var array = new JsArray
      for (detail <- details) {
        array = array.prepend(Json.obj(detail._1 -> detail._2))
      }
      array
    }

    def asJson(): JsArray = {
      var array = new JsArray
      for ((actor, details) <- map) {
        val actorJson = Json.obj("name" -> actor.name,
          "posterUrl" -> actor.posterUrl,
          "entityCount" -> getEntityCountAsJson(details))
        array = array.prepend(actorJson)
      }
      array
    }
    val json = Json.obj("count" -> map.size,
      "" -> asJson)

    json
  }
}