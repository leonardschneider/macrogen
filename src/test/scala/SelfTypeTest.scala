package macrogen.test

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SelfTypeTest extends SpecificationWithJUnit {

  "SelfType" should {

    "get self type for a simple class" in {
      import macrogen.SelfType._

      class A extends SelfType
      val a = new A
      implicitly[a.Self =:= A]
      true must beEqualTo(true)
    }

    "get self type with a full name for SelfType" in {
      import macrogen.SelfType._

      class A extends macrogen.SelfType.SelfType
      val a = new A
      implicitly[a.Self =:= A]
      true must beEqualTo(true)
    }

    "get self type for a higher kind" in {
      import macrogen.SelfType._

      class A[X] extends macrogen.SelfType.SelfType
      val a = new A[Int]
      implicitly[a.Self[Int] =:= A[Int]]
      true must beEqualTo(true)
    }

  }

}


