package macrogen.test

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CacheTest extends SpecificationWithJUnit {

  "Cache macro" should {
  
    "cache function result efficiently" in {
      import macrogen.cached
      import macrogen.Util._

      def fac(i: BigInt): BigInt = if(i<2) 1 else i*fac(i-1)

      def facCached(i: BigInt): BigInt = cached { if(i < 2) 1 else i * facCached(i - 1) }

      timeOf { (1 to 10000).foreach(_ => fac(200)) } must 
        beGreaterThan(timeOf { (1 to 10000).foreach(_ => facCached(200)) })
    
    }

  }

}

