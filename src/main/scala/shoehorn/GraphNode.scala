package shoehorn

import blueeyes.json.xschema.Decomposer
import blueeyes.json.JsonAST._
import blueeyes.json.JsonAST.JString
import blueeyes.json.JsonAST.JField

case class GraphNode(id: String, weight: Int, links: List[Link], webTitle: String, webUrl: String)

case class Link(id: String, tags: List[String], length: Int)

object GraphNode {

  implicit val NodeDecomposer: Decomposer[GraphNode] = new Decomposer[GraphNode] {
    def decompose(node: GraphNode): JValue = JObject(List(
      JField("id", JString(node.id)),
      JField("weight", JInt(node.weight)),
      JField("webTitle", JString(node.webTitle)),
      JField("webUrl", JString(node.webUrl)),
      JField("links", JArray(node.links map {
        link => JObject(List(
          JField("id", JString(link.id)),
          JField("tags", JArray(link.tags map (JString(_: String)))),
          JField("length", JInt(link.length))
        ))
      }))
    ))
  }

}
