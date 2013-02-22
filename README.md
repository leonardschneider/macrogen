macrogen
========

Experimentation of scala macro paradise for utility macros.

Any feedback or comment is welcome.

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

SelfType
--------

When mixing the SelfType trait, a type `Self <:` the inheriting trait is automatically declared.  


