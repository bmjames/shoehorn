package shoehorn

import akka.dispatch.Future
import scalaz.Scalaz._

import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.data.{BijectionsChunkJson}
import blueeyes.core.http.MimeTypes.{json, application}
import blueeyes.core.http.HttpResponse
import blueeyes.json.JsonAST.JValue

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
            get { _: Req =>

              val latestContent = client.latest(50)

              latestContent map { contents =>

                val tagToContent: Map[Tag, List[Content]] = contents foldMap { content =>
                  content.tags foldMap ( tag => Map(tag -> List(content)))
                }

                val interestingTags = tagToContent filter { case (_, content) => content.length > 1 }

                val nodes = contents map { content =>
                  val links = content.tags flatMap {
                    tag => interestingTags.getOrElse(tag, Nil) map(content => Link(content.id, tag.id))
                  }
                  GraphNode(content.id, links)
                }

                HttpResponse[JValue](content = Some(nodes.serialize))
              }

            }
          }
        }
      } ->
      shutdown(_ => Future(println("Shutting down...")))
  }}

}
