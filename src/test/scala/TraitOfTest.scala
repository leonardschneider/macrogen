package macrogen.test

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TraitOfTest extends SpecificationWithJUnit {

  "TraitOf type" should {
    
    "make trait of a class" in {
      import macrogen.TraitOf._

      class AImpl {
        def f(i: Int): Int = i + 42
        val x = 81
        protected val xx = 32
        var y = 2
        type X = Int
        class B
        trait C
        object o { val i = 77 }
      }
      
      // types, object, class traits must be declared explicitly for now
      trait ATrait extends TraitOf[AImpl] {
        type X <: Int
      }

      class A extends AImpl with ATrait

      val a: ATrait = new A
      
      a.f(1) must beEqualTo(43)
      a.x must beEqualTo(81)
      a.y must beEqualTo(2)
      a.y = 55
      a.y must beEqualTo(55)

    }
  
  }

}

