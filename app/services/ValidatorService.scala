package services

import com.google.inject.ImplementedBy
import models.SaxParseError
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

import java.io.StringReader
import java.net.URL
import javax.inject.Inject
import javax.xml.parsers.{SAXParser, SAXParserFactory}
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import scala.collection.mutable.ListBuffer
import scala.xml.Elem

class XMLValidationService @Inject()(xmlValidatingParser: XMLValidatingParser){
  def validateXml(xmlString: String): Either[ListBuffer[SaxParseError], Elem] = {
    val list: ListBuffer[SaxParseError] = new ListBuffer[SaxParseError]

    trait AccumulatorState extends DefaultHandler {
      override def warning(e: SAXParseException): Unit = list += SaxParseError(e.getLineNumber, e.getMessage)

      override def error(e: SAXParseException): Unit = list += SaxParseError(e.getLineNumber, e.getMessage)

      override def fatalError(e: SAXParseException): Unit = list += SaxParseError(e.getLineNumber, e.getMessage)
    }

    val elem = new scala.xml.factory.XMLLoader[scala.xml.Elem] {
      override def parser: SAXParser = xmlValidatingParser.validatingParser

      override def adapter =
        new scala.xml.parsing.NoBindingFactoryAdapter
          with AccumulatorState


    }.load(new StringReader(xmlString))

    if(list.nonEmpty) Left(list)
    else Right(elem)
  }

  def validateManualSubmission(xml: Elem): ListBuffer[SaxParseError] ={

    val list: ListBuffer[SaxParseError] = new ListBuffer[SaxParseError]

    trait AccumulatorState extends DefaultHandler {
      override def warning(e: SAXParseException): Unit = list += SaxParseError(e.getLineNumber, e.getMessage)

      override def error(e: SAXParseException): Unit = list += SaxParseError(e.getLineNumber, e.getMessage)

      override def fatalError(e: SAXParseException): Unit = list += SaxParseError(e.getLineNumber, e.getMessage)
    }

    new scala.xml.factory.XMLLoader[scala.xml.Elem] {
      override def parser: SAXParser = xmlValidatingParser.validatingParser

      override def adapter =
        new scala.xml.parsing.NoBindingFactoryAdapter
          with AccumulatorState


    }.load(new StringReader(xml.mkString))
    list
  }
}

@ImplementedBy(classOf[XMLDacXSDValidatingParser])
trait XMLValidatingParser {
  def validatingParser: SAXParser
}


class XMLDacXSDValidatingParser extends XMLValidatingParser {
  val schemaLang: String = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI
  val isoXsdUrl: URL = getClass.getResource("/schemas/IsoTypes_v1.01.xsd")
  val ukDAC6XsdUrl: URL = getClass.getResource("/schemas/UKDac6XSD_v0.5.xsd")
  val ukDCT06XsdUrl: URL = getClass.getResource("/schemas/DCT06_EIS_UK_schema.xsd")
  val isoXsdStream: StreamSource = new StreamSource(isoXsdUrl.openStream())
  val ukDAC6XsdStream: StreamSource = new StreamSource(ukDAC6XsdUrl.openStream())
  val ukDCT06XsdStream: StreamSource = new StreamSource(ukDCT06XsdUrl.openStream())

  //IsoTypes xsd is referenced by UKDac6XSD so must come first in the array
  val streams: Array[Source] = Array(isoXsdStream, ukDAC6XsdStream, ukDCT06XsdStream)

  val schema: Schema = SchemaFactory.newInstance(schemaLang).newSchema(streams)

  val factory: SAXParserFactory = SAXParserFactory.newInstance()
  factory.setNamespaceAware(true)
  factory.setSchema(schema)

  override def validatingParser: SAXParser = factory.newSAXParser()
}
