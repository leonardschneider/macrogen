package macrogen.test

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import macrogen.TupleImplicit._

@RunWith(classOf[JUnitRunner])
class TupleTest extends SpecificationWithJUnit {

  "Tuple ops" should {
  
    "prepend element" in {
      1 :: ("two", 3.0) must beEqualTo((1, "two", 3.0))
    }
    
    "append element" in {
      (1, "two") :+ 3.0 must beEqualTo((1, "two", 3.0))
    }

    "append tuple" in {
      (1, "two", 3.0) ++ mkTuple((4, "five", 6.0)) must beEqualTo((1, "two", 3.0, 4, "five", 6.0))
    }

    "get head" in {
      //FIXME I don't know why implicit macro fails to convert here
      mkTuple((1, "two", 3.0)).head must beEqualTo(1)
      //(1, "two", 3.0).head must beEqualTo(1)
    }

    "get tail" in {
      //FIXME I don't why implicit macro fails to convert here
      mkTuple((1, "two", 3.0)).tail must beEqualTo(("two", 3.0))
    }

    "reverse" in {
      mkTuple((1, "two", 3.0)).reverse must beEqualTo((3.0, "two", 1))
    }

    "get length" in {
      mkTuple((1, "two", 3.0)).length must beEqualTo(3)
      //(1, "two", 3.0).length must beEqualTo(3)
    }

    "operate a lambda expression" in {
      (1, 2.0, 3f).operate(_ + _ * _) must beEqualTo(7.0)
    }

    "map a lambda expression" in {
      mkTuple((1, 2.0, 3f)).map(_ + 1) must beEqualTo((2, 3.0, 4f))
    }

    "reduce left with a lambda expression" in {
      mkTuple((1, 2.0, 3f, 4)).reduceLeft(_ + _) must beEqualTo(10.0)
    }

    "reduce right with a lambda expression" in {
      mkTuple((1, 2.0, 3f, 4)).reduceRight(_ - _) must beEqualTo(-2.0)
    }
    

  }

}
