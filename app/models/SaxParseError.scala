package models

import play.api.libs.json.Json

case class SaxParseError(lineNumber: Int, errorMessage: String)

object SaxParseError {
  implicit val format = Json.format[SaxParseError]
}