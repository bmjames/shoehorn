package shoehorn

import akka.dispatch.Future
import scalaz.Scalaz._
import scalaz.State

import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.data.{BijectionsChunkString, BijectionsChunkFutureJson, BijectionsChunkJson, ByteChunk}
import blueeyes.core.http.MimeTypes.{json, application}
import blueeyes.core.http.{HttpRequest, HttpResponse}
import blueeyes.json.JsonAST._
import blueeyes.json.xschema.SerializationImplicits._
import blueeyes.json.xschema.DefaultDecomposers._

import GraphNode._


trait ShoehornService extends BlueEyesServiceBuilder {

  import BijectionsChunkString._
  import BijectionsChunkJson._
  import BijectionsChunkFutureJson._

  val shoehorn = service("shoehorn", "1.0.0") { requestLogging { context =>
    startup {
      Future(new ContentApi(context.config[String]("contentApiUrl")))
    } ->
    request { client: ContentApi =>
      produce(application/json) {
        path("/") {
          jsonp[ByteChunk] { req: HttpRequest[Future[JValue]] =>

            val ignoredTags = req.parameters.get('ignore) map (_.split(',').toSet.map(Tag.apply))
            val ignoreSectionTags = req.parameters.contains(Symbol("ignore-section-tags"))

            for {
              latestContent <- client.latest(40)
              nodes = buildGraph(latestContent, ignoredTags.orZero, ignoreSectionTags)
            } yield {
              HttpResponse[JValue](content = Some(wrapResponse(nodes)))
            }

          }
        }
      }
    } ->
    shutdown(_ => Future(println("Shutting down...")))
  }}

  def wrapResponse(nodes: List[GraphNode]): JValue =
    JObject(List(JField("nodes", nodes.serialize)))

  def buildGraph(contents: List[Content], ignoredTags: Set[Tag], ignoreSectionTags: Boolean): List[GraphNode] = {
    val tagToContent: Map[Tag, List[Content]] = contents foldMap { content =>
      content.tags foldMap (tag => Map(tag -> List(content)))
    } filterKeys noIgnoredPrefix(ignoredTags) filterKeys (key => ! (ignoreSectionTags && isSectionTag(key)))
    contents map { content => toGraphNode(content, tagToContent) } filter (_.links.nonEmpty)
  }

  def toGraphNode(content: Content, tagToContent: Map[Tag, List[Content]]): GraphNode = {
    val linkedContent = content.tags foldMap { tag =>
      tagToContent.getOrElse(tag, Nil) filter (_.id != content.id) foldMap { linkedContent =>
        Map(linkedContent.id -> List(tag))
      }
    }
    val (weight, links) = linkedContent.toList.runTraverseS(0)(link _ tupled)
    GraphNode(content.id, weight, links, content.webTitle)
  }

  def link(contentId: String, tags: List[Tag]): State[Int, Link] =
    modify[Int](_ + tags.length) >| Link(contentId, tags map (_.id), math.max(1, 10 / tags.length))

  def noIgnoredPrefix(ignoredTags: Set[Tag])(tag: Tag): Boolean =
    ! ignoredTags.exists(tag.parts startsWith _.parts)

  def isSectionTag(tag: Tag): Boolean =
    PartialFunction.cond(tag.parts) {
      case Array(p1, p2) => p1 == p2
    }
}
