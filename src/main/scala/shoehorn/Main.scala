package shoehorn

import blueeyes.BlueEyesServer

object Main extends BlueEyesServer with ShoehornService {

  override def main(args: Array[String]) {
    super.main(Array("--configFile", "default.conf"))
  }

}
