import scala.util.Random

package object utilities {
  def buildBlock(size: Int): String = {
    //  Creates random ascii string
    (for (_ <- 0 until 10)
      yield Random.between(65, 90).asInstanceOf[Char]
      ).mkString
  }
  def getRandom[A](seq: Seq[A]): A = seq(Random.between(0, seq.length))
}
