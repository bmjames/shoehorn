package shoehorn

import blueeyes.core.data.{BijectionsChunkString, BijectionsChunkJson}
import blueeyes.BlueEyesServiceBuilder
import akka.dispatch.{Promise, Future}
import blueeyes.core.http.MimeTypes.{text, plain, json, application}
import blueeyes.core.http.HttpResponse

trait ShoehornService extends BlueEyesServiceBuilder with BijectionsChunkString {


  val shoehorn = service("shoehorn", "1.0.0") { context =>
    startup {
      Future(())
    } ->
      request { config: Unit =>
        produce(text/plain) {
          path("/") {
            get { _: Req =>

              Future(HttpResponse[String](content = Some("hello, world!")))

            }
          }
        }
      } ->
      shutdown(_ => Future(()))
  }

}
