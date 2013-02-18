import blueeyes.core.data._
import blueeyes.core.http.HttpRequest

package object shoehorn {

  type Req = HttpRequest[ByteChunk]

}
