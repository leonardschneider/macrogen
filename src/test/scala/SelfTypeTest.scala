package macrogen.test

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SelfTypeTest extends SpecificationWithJUnit {

  "SelfType" should {

    "get self type" in {
      import macrogen.SelfType._

      class A extends SelfType
      val a = new A
      implicitly[a.Self =:= A]
      true must beEqualTo(true)
    }

  }

}


