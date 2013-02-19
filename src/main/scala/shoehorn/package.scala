
import blueeyes.core.data.{BijectionsChunkFutureJson, BijectionsChunkJson, BijectionsChunkString}
import java.net.URLEncoder

package object shoehorn {

  def urlEncode(s: String): String = URLEncoder.encode(s, "utf-8")

}
