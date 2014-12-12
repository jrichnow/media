package utils

import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue

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
}