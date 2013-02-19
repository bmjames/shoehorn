package shoehorn

import akka.dispatch.Future
import scalaz.Scalaz._

import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.data.{BijectionsChunkJson}
import blueeyes.core.http.MimeTypes.{json, application}
import blueeyes.core.http.HttpResponse
import blueeyes.json.JsonAST._

import blueeyes.json.xschema.SerializationImplicits._
import blueeyes.json.xschema.DefaultDecomposers._
import GraphNode._


trait ShoehornService extends BlueEyesServiceBuilder with BijectionsChunkJson {

  val shoehorn = service("shoehorn", "1.0.0") { requestLogging { context =>
    startup {
      Future(new ContentApi(context.config[String]("contentApiUrl")))
    } ->
      request { client: ContentApi =>
        produce(application/json) {
          path("/") {
            get { req: Req =>

              val ignoredTags = req.parameters.get('ignore) map (_.split(',').toSet.map(Tag.apply))

              for {
                latestContent <- client.latest(50)
                nodes = buildGraph(latestContent, ignoredTags.orZero)
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

  def buildGraph(contents: List[Content], ignoredTags: Set[Tag]): List[GraphNode] = {
    val tagToContent: Map[Tag, List[Content]] = contents foldMap { content =>
      content.tags foldMap (tag => Map(tag -> List(content)))
    } filterKeys (tag => ! ignoredTags(tag))
    contents map { content => toGraphNode(content, tagToContent) }
  }

  def toGraphNode(content: Content, tagToContent: Map[Tag, List[Content]]): GraphNode = {
    val linkedContent = content.tags foldMap { tag =>
      tagToContent.getOrElse(tag, Nil) filter (_.id != content.id) foldMap { linkedContent =>
        Map(linkedContent.id -> List(tag))
      }
    }
    val links = linkedContent.toList map { case (contentId, tags) =>
      val length = math.max(1, 10 / tags.length)
      Link(contentId, tags map (_.id), length)
    }
    GraphNode(content.id, links, content.webTitle)
  }

}
