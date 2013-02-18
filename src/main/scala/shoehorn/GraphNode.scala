package shoehorn

case class GraphNode(id: String, links: List[Link])

case class Link(id: String, tag: String)
