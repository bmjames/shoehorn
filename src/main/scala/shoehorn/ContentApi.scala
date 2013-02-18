package shoehorn

import org.joda.time.{DateTimeZone, DateTime}
import akka.dispatch.Future
import blueeyes.core.data.BijectionsChunkJson
import blueeyes.core.service.engines.HttpClientXLightWeb
import blueeyes.json.JsonAST._
import blueeyes.json.JsonAST.JString

import org.joda.time.format.ISODateTimeFormat

final class ContentApi(url: String) extends BijectionsChunkJson {
  import ContentApi._

  val client = (new HttpClientXLightWeb).path(url).translate[JValue]

  val defaultParams = List(
    "user-tier" -> "internal",
    "show-tags" -> "all"
  )

  def search(params: (String, String)*): Future[List[Content]] = {
    val allParams = params ++ defaultParams
    val url = "search.json?" +
      (allParams map { case (k, v) => urlEncode(k) + "=" + urlEncode(v) } mkString "&")
    client.get[JValue](url) map (o => o.content map parseResults getOrElse Nil)
  }

  def latest(limit: Int): Future[List[Content]] =
    search("page-size" -> limit.toString)

}

object ContentApi {

  def parseResults(json: JValue): List[Content] =
    (for (JArray(results) <- json \\ "results") yield (results flatMap parseContent)).flatten

  def parseContent(json: JValue): Option[Content] =
    for {
      JString(id) <- json \? "id"
      JString(webPubDate) <- json \? "webPublicationDate"
      JArray(tags) <- json \? "tags"
    } yield {
      Content(id, parseDateTime(webPubDate), tags flatMap parseTag)
    }

  def parseTag(json: JValue): Option[Tag] =
    json \? "id" collect { case JString(id) => Tag(id) }

  def formatDateTime(dt: DateTime): String =
    ISODateTimeFormat.dateTimeNoMillis.withZone(DateTimeZone.UTC) print dt

  def parseDateTime(dt: String): DateTime =
    ISODateTimeFormat.dateTimeNoMillis.parseDateTime(dt)

}


case class Content(id: String, webPubDate: DateTime, tags: List[Tag])

case class Tag(id: String)
