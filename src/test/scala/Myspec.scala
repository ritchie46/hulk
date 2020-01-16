import org.scalatest.flatspec.AnyFlatSpec

abstract class UnitSpec extends AnyFlatSpec

class MySpec extends UnitSpec {
  "A header combination" should "be shuffled" in {
    val h = new http.Headers("localhost")
    val a = for (_ <- 0 to 9)
      yield h.generate != h.generate
    assert(a.contains(true))
  }
}

