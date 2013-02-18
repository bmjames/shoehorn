package shoehorn

import blueeyes.core.data.{BijectionsChunkString, BijectionsChunkJson}
import blueeyes.BlueEyesServiceBuilder
import akka.dispatch.Future
import blueeyes.core.http.MimeTypes.{text, plain, json, application}
import blueeyes.core.http.HttpResponse


trait ShoehornService extends BlueEyesServiceBuilder with BijectionsChunkString {


  val shoehorn = service("shoehorn", "1.0.0") { requestLogging { context =>
    startup {
      Future(new ContentApi(context.config[String]("contentApiUrl")))
    } ->
      request { client: ContentApi =>
        produce(text/plain) {
          path("/") {
            get { _: Req =>

              val latestContent = client.latest(10)

              latestContent map { contents =>
                val bodyText = contents mkString "\n"
                HttpResponse[String](content = Some(bodyText))
              }

            }
          }
        }
      } ->
      shutdown(_ => Future(println("Shutting down...")))
  }}

}
