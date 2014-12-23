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
        array = array.prepend(Json.obj("name" -> detail._1, "count" -> detail._2))
      }
      array
    }
    
    def shortBiography(fullBiography: Option[String]):String = {
      fullBiography match {
        case None => ""
        case Some(a) if a.length < 300 => a
        case Some(b) => b.substring(0,300) + " ..."
      }
    }

    def asJson(): JsArray = {
      var array = new JsArray
      for ((actor, details) <- map) {
        val actorJson = Json.obj("name" -> actor.name,
          "birthDay" -> actor.birthDay,
          "biography" -> shortBiography(actor.biography),
          "posterUrl" -> actor.posterUrl,
          "entityCount" -> getEntityCountAsJson(details))
        array = array.prepend(actorJson)
      }
      array
    }
    val json = Json.obj("count" -> map.size,
      "results" -> asJson)

    json
  }
}