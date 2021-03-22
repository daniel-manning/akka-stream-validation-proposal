package controllers


import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.libs.streams.Accumulator
import play.api.mvc._
import services.XMLValidationService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionController @Inject()(
                                     val controllerComponents: ControllerComponents,
                                     validationService: XMLValidationService
                                   )(implicit ec: ExecutionContext) extends BaseController with BodyParserUtils {

  def submitDisclosure = Action.async(submitParser) {
    implicit request => {
      validationService.validateXml(???)
      Future.successful(Ok(""))
    }
  }

  private def submitParser: BodyParser[Source[ByteString, _]] = {
    when(
      _.contentType.exists { t =>
        val tl = t.toLowerCase()
        tl.startsWith("text/xml") || tl.startsWith("application/xml")
      },
      streamingParser,
      _ => Future.successful(UnsupportedMediaType("Unsupported Content-Type"))
    )
  }

  private val streamingParser: BodyParser[Source[ByteString, _]] = BodyParser { _ =>
    Accumulator.source[ByteString]
      .map(Right.apply)
  }
}
