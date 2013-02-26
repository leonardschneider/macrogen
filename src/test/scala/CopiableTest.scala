package macrogen.test

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopiableTest extends SpecificationWithJUnit {

  "Copiable trait" should {
  
    "work with a simple class" in {
      import macrogen.Copiable._
      import macrogen.CopyConstructor

      class A extends Copiable {
        var i = 42
        val x = 2
        def f(j: Int) = i + j
      }

      val a = new A
      a.i = 81
      val a2 = a.copy

      a2.i must beEqualTo(a.i)

    }

    "work with a simple trait" in {
      import macrogen.Copiable._

      trait A extends Copiable {
        val s: String
        var i = 42
        val x = 2
        def f(j: Int) = i + j
      }

      val a = new A { val s = "A" }
      a.i = 81
      val a2 = a.copy

      a2.i must beEqualTo(a.i)
   
    
    }

    "work with simple trait inheritance" in {
      import macrogen.Copiable._

      trait A extends Copiable {
        //val s: String
        var i = 42
        val x = 2
        //def f(j: Int) = i + j
        //def g(j: Int): String
      }

      class B extends A { //with Copiable {
        //val s = "B"
        //def g(j: Int) = s + j
      }

      val b = new B
      b.i = 81
      val b2 = b.copy

      b2.i must beEqualTo(b.i)

    }
  
  }

}

