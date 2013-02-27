package macrogen

import scala.language.implicitConversions

import language.experimental.macros
import scala.reflect.macros.Macro

trait TupleMacros extends Macro {
  import c.universe._

  def ::[Tup: WeakTypeTag, T: WeakTypeTag](t: Expr[T]) = {
    val length = weakTypeOf[Tup].typeSymbol.asClass.typeParams.length
    val fields0 =
      weakTypeOf[Tup].members.
      filter(t => t.isTerm && t.asTerm.isCaseAccessor && t.asTerm.isGetter).
      toList.sortBy(_.fullName).
      map(f => q"${c.prefix.tree}.tup.${f.name.toTermName}")
    c.echo(NoPosition, "fields0 " + fields0)
    val tup = definitions.TupleClass(length + 1).name.toTermName
    val fields = q"${t.tree}" +: fields0
    c.echo(NoPosition, "fields " + fields)
    val tree = c.typeCheck(q"$tup(..$fields)")
    c.echo(NoPosition, "tup :: tree " + tree)
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def mkTuple[Tup: WeakTypeTag](tup: Expr[Tup]): Expr[Tuple[Tup]] = {
    // get the tuple symbol tupleX
    val tupSymOption = weakTypeOf[Tup].baseClasses.find(_.fullName.matches("scala.Tuple[0-9]+"))
    val tupSym = tupSymOption.getOrElse(sys.error("Not a tuple"))
    reify(new Tuple[Tup](tup.splice))
  }

}

class Tuple[Tup](val tup: Tup) {
  def ::[T](t: T): Any = macro TupleMacros.::[Tup, T]

}

object TupleImplicit {
  implicit def mkTuple[Tup](tup: Tup): Tuple[Tup] = macro TupleMacros.mkTuple[Tup]
}

