
import java.net.URLEncoder

import blueeyes.core.data._
import blueeyes.core.http.HttpRequest

package object shoehorn {

  type Req = HttpRequest[ByteChunk]

  def urlEncode(s: String): String = URLEncoder.encode(s, "utf-8")

}
