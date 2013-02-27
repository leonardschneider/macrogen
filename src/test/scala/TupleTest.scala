package macrogen.test

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import macrogen.TupleImplicit._

@RunWith(classOf[JUnitRunner])
class TupleTest extends SpecificationWithJUnit {

  "Tuple" should {
  
    "allow prepending" in {
      1 :: ("two", 3.0) must beEqualTo((1, "two", 3.0))
    }

  }

}
