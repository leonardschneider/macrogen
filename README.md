macrogen
========

**Work in progress**

Experimentation of scala macro paradise for utility macros.

Any feedback or comment is welcome.

Tuple
-----

**WARNING: there are still a few bugs to be ironed out**

`macrogen` provides collection like methods on tuples (`head`, `tail`, `reverse`, `::`, `:+`, `toList`, `map`, `reduceLeft`, `reduceRight`, `foldLeft`, `foldRight`).

In addition, some utility methods are provided for numeric computation: `|+|`, `|-|`, `|*|`, `|/|`.

The `operate` method allows applying a given function on the tuple.

Lambda expressions, polymorphic or overloaded functions can be passed to `operate`, `map`, `reduceLeft`, `reduceRight`, `foldLeft` and `foldRight`.

Example REPL session.

```scala
scala> import macrogen.TupleImplicit._
import macrogen.TupleImplicit._

scala> val tup = (1, "two") :+ 3.0
tup: (Int, String, Double) = (1,two,3.0)

scala> val tup = (1, 2.0) :+ 3f
tup: (Int, Double, Float) = (1,2.0,3.0)

scala> tup.operate(_ * _ + _)
res1: Double = 5.0

scala> tup.reduceLeft(_ + _)
res2: Double = 6.0

scala> tup.map(_ + 1)
res3: (Int, Double, Float) = (2,3.0,4.0)

scala> tup.reverse
res4: (Float, Double, Int) = (3.0,2.0,1)
```

TraitOf
-------

The `TraitOf` generic trait allows building a trait from an implementation class. It will export any public val, var or def into inheriting trait. It helps you remove some boilerplate.

e.g. from test specs
```scala
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

// types, object, class traits must be declared explicitly
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
```

Function Cached Macro
---------------------

For (pure) function memoization, you can use the cached macro just before the function definition, as
show after.

```scala
scala> import macrogen.cached
import macrogen.cached

scala> import macrogen.Util._
import Util._

scala> def fac(i: BigInt): BigInt = if(i<2) 1 else i*fac(i-1)
fac: (i: BigInt)BigInt

scala> def facCached(i: BigInt): BigInt = cached { if(i < 2) 1 else i * facCached(i - 1) }
facCached: (i: BigInt)BigInt

scala> time { (1 to 10000).foreach(_ => fac(200)) }
471409 microseconds

scala> time { (1 to 10000).foreach(_ => facCached(200)) }
14720 microseconds
```


SelfType
--------

When mixing the SelfType trait, a type `Self <:` the inheriting trait is automatically declared.  


Type Operators
--------------

Type operators complete usual type relationship testing, such as `=:=`, `<:<`, `>:>`, `<%<` with
their negation `=:!=`, `<:!<`, `>:!>`, `<%!<`.

It also brings way to combine those thanks to `!:!` (type negation), `&:&` and `|:|`.

Example RELP session.

```scala
scala> import macrogen.TypeOperators._
import macrogen.TypeOperators._

scala> trait A
defined trait A

scala> trait B
defined trait B

scala> implicitly[A =:!= B]
res0: macrogen.TypeOperators.=:!=[A,B] = $anon$1@1106d26c

scala> type A_alias = A
defined type alias A_alias

scala> implicitly[A =:!= A_alias] // oops
<console>:13: error: could not find implicit value for parameter e: macrogen.TypeOperators.=:!=[A,A_alias]
              implicitly[A =:!= A_alias] // oops

```


