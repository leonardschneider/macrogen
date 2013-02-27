package macrogen

import scala.language.implicitConversions
import scala.language.higherKinds

import language.experimental.macros
import scala.reflect.macros.Macro

trait TupleMacros extends Macro {
  import c.universe._

  // Helpers

  class ops[Tup: WeakTypeTag](tup0: Tree, tupName: TermName) {
    def length = weakTypeOf[Tup].typeSymbol.asClass.typeParams.length
    def fields: List[Tree] = 
      weakTypeOf[Tup].members.
      filter(t => t.isTerm && t.asTerm.isCaseAccessor && t.asTerm.isGetter).
      toList.sortBy(_.fullName).
      map(f => q"$tupName.${f.name.toTermName}")
    def ctor(l: Int): TermName = definitions.TupleClass(l).name.toTermName
    def ctor: TermName = ctor(length)
    def decl = q"val $tupName = $tup0.tup" // caching prefix
    def tpes = {
      val TypeRef(_, _, res) = weakTypeOf[Tup]
      res
    }
  }
  object ops {
    def apply[Tup: WeakTypeTag] = new ops[Tup](c.prefix.tree, TermName("tup"))
    def apply[Tup: WeakTypeTag](tup: Expr[Tuple[Tup]], s: String) = new ops[Tup](tup.tree, TermName(s))
  }

  // Core methods

  def ::[Tup: WeakTypeTag, T: WeakTypeTag](t: Expr[T]) = {
    val fields = q"${t.tree}" +: ops[Tup].fields
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${ops[Tup].ctor(ops[Tup].length + 1)}(..$fields)}")
    //c.echo(NoPosition, "tup :: tree " + tree)
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def :+[Tup: WeakTypeTag, T: WeakTypeTag](t: Expr[T]) = {
    val fields = ops[Tup].fields :+ q"${t.tree}"
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${ops[Tup].ctor(ops[Tup].length + 1)}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def ++[Tup: WeakTypeTag, Tup2: WeakTypeTag](tup2: c.Expr[Tuple[Tup2]]) = {
    val tup2ops = ops(tup2, "tup2")
    val fields = ops[Tup].fields ++ tup2ops.fields
    val length = fields.length
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${tup2ops.decl}; ${ops[Tup].ctor(length)}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def head[Tup: WeakTypeTag] = {
    val tree = c.typeCheck(q"${c.prefix.tree}.tup._1")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def tail[Tup: WeakTypeTag] = {
    val fields = ops[Tup].fields.tail
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${ops[Tup].ctor(ops[Tup].length - 1)}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def reverse[Tup: WeakTypeTag] = {
    val fields = ops[Tup].fields.reverse
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${ops[Tup].ctor}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def length[Tup: WeakTypeTag] =
    c.literal(ops[Tup].length)

  def toList[Tup: WeakTypeTag] = {
    val fields = ops[Tup].fields
    val tree = c.typeCheck(q"{${ops[Tup].decl}; List(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def |+|[Tup: WeakTypeTag, Tup2: WeakTypeTag](tup2: Expr[Tuple[Tup2]]) = {
    val tup2ops = ops(tup2, "tup2")
    c.echo(NoPosition, "length " + tup2ops.length)
    require(tup2ops.length == ops[Tup].length, s"Tuples of different size (${ops[Tup].length} expected)")
    val fields = (ops[Tup].fields zip tup2ops.fields).map{case (f1, f2) => q"$f1 + $f2"}
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${tup2ops.decl}; ${ops[Tup].ctor}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }
  
  def |-|[Tup: WeakTypeTag, Tup2: WeakTypeTag](tup2: Expr[Tuple[Tup2]]) = {
    val tup2ops = ops(tup2, "tup2")
    c.echo(NoPosition, "length " + tup2ops.length)
    require(tup2ops.length == ops[Tup].length, s"Tuples of different size (${ops[Tup].length} expected)")
    val fields = (ops[Tup].fields zip tup2ops.fields).map{case (f1, f2) => q"$f1 - $f2"}
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${tup2ops.decl}; ${ops[Tup].ctor}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }
  
  def |*|[Tup: WeakTypeTag, Tup2: WeakTypeTag](tup2: Expr[Tuple[Tup2]]) = {
    val tup2ops = ops(tup2, "tup2")
    c.echo(NoPosition, "length " + tup2ops.length)
    require(tup2ops.length == ops[Tup].length, s"Tuples of different size (${ops[Tup].length} expected)")
    val fields = (ops[Tup].fields zip tup2ops.fields).map{case (f1, f2) => q"$f1 * $f2"}
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${tup2ops.decl}; ${ops[Tup].ctor}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def |/|[Tup: WeakTypeTag, Tup2: WeakTypeTag](tup2: Expr[Tuple[Tup2]]) = {
    val tup2ops = ops(tup2, "tup2")
    c.echo(NoPosition, "length " + tup2ops.length)
    require(tup2ops.length == ops[Tup].length, s"Tuples of different size (${ops[Tup].length} expected)")
    val fields = (ops[Tup].fields zip tup2ops.fields).map{case (f1, f2) => q"$f1 / $f2"}
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${tup2ops.decl}; ${ops[Tup].ctor}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def operate[Tup: WeakTypeTag, App: WeakTypeTag](f: Expr[App]) = {
    val fDecl = q"val f0 = ${f.tree}"
    val tree = c.typeCheck(q"{${ops[Tup].decl}; $fDecl; f0(..${ops[Tup].fields})}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def map[Tup: WeakTypeTag](f: Tree) = {
    val fields = (ops[Tup].fields zip ops[Tup].tpes).map{ case (tree, tpe) => 
      q"macrogen.InferFunction1[$tpe].infer(${f.duplicate})($tree)"
    }
    val tree = c.typeCheck(q"{${ops[Tup].decl}; ${ops[Tup].ctor}(..$fields)}")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def reduceLeft[Tup: WeakTypeTag](f: Tree) = {
    val (tree, tpe) = (ops[Tup].fields zip ops[Tup].tpes).reduceLeft((t1, t2) => {
      val (tree1, tpe1) = t1
      val (tree2, tpe2) = t2
      val tree = q"macrogen.InferFunction2[$tpe1, $tpe2].infer(${f.duplicate})($tree1, $tree2)"
      val tpe = c.typeCheck(q"{${ops[Tup].decl}; ${tree.duplicate}}").tpe
      (tree, tpe)
    })
    c.Expr(q"{${ops[Tup].decl}; $tree}")(c.WeakTypeTag(tpe))
  }

  def reduceRight[Tup: WeakTypeTag](f: Tree) = {
    val (tree, tpe) = (ops[Tup].fields zip ops[Tup].tpes).reduceRight((t1, t2) => {
      val (tree1, tpe1) = t1
      val (tree2, tpe2) = t2
      val tree = q"macrogen.InferFunction2[$tpe1, $tpe2].infer(${f.duplicate})($tree1, $tree2)"
      val tpe = c.typeCheck(q"{${ops[Tup].decl}; ${tree.duplicate}}").tpe
      (tree, tpe)
    })
    c.Expr(q"{${ops[Tup].decl}; $tree}")(c.WeakTypeTag(tpe))
  }


  // Implicit conversion macro implementation

  def mkTuple[Tup: WeakTypeTag](tup: Expr[Tup]): Expr[Tuple[Tup]] = {
    // get the tuple symbol tupleX
    val tupSymOption = weakTypeOf[Tup].baseClasses.find(_.fullName.matches("scala.Tuple[0-9]+"))
    val tupSym = tupSymOption.getOrElse(sys.error("Not a tuple"))
    val targs = ops[Tup].tpes.map(t => tq"$t") :+ tq"R"
    val fun = definitions.FunctionClass(ops[Tup].length).name.toTypeName 
    val tree = c.typeCheck(q"new macrogen.Tuple(${tup.tree}) { type Application[R] = $fun[..$targs] }")
    c.Expr(tree)(c.WeakTypeTag(tree.tpe))
  }

  def mkArgs = {
    
  }

  //override def onInfer(tic: c.TypeInferenceContext): Unit = {
  //  c.echo(NoPosition, "Def" + c.enclosingMacros)
  //  tic.inferDefault
  //}

}

class Tuple[Tup](val tup: Tup) {
  type Application[R]
  def ::[T](t: T): Any = macro TupleMacros.::[Tup, T]
  def :+[T](t: T): Any = macro TupleMacros.:+[Tup, T]
  def ++[Tup2](tup2: Tuple[Tup2]): Any = macro TupleMacros.++[Tup, Tup2]
  def head: Any = macro TupleMacros.head[Tup]
  def tail: Any = macro TupleMacros.tail[Tup]
  def reverse: Any = macro TupleMacros.reverse[Tup]
  def length: Int = macro TupleMacros.length[Tup]
  def toList: Any = macro TupleMacros.toList[Tup]
  def |+|[Tup2](tup2: Tuple[Tup2]): Any = macro TupleMacros.|+|[Tup, Tup2]
  def |-|[Tup2](tup2: Tuple[Tup2]): Any = macro TupleMacros.|-|[Tup, Tup2]
  def |*|[Tup2](tup2: Tuple[Tup2]): Any = macro TupleMacros.|*|[Tup, Tup2]
  def |/|[Tup2](tup2: Tuple[Tup2]): Any = macro TupleMacros.|/|[Tup, Tup2]
  def operate[R](f: Application[R]): R = macro TupleMacros.operate[Tup, Application[R]]
  def map(f: _): Any = macro TupleMacros.map[Tup]
  def reduceLeft(f: _): Any = macro TupleMacros.reduceLeft[Tup]
  def reduceRight(f: _): Any = macro TupleMacros.reduceRight[Tup]
  //def foldLeft(f: _): Any = macro TupleMacros.foldLeft[Tup]
  //def foldRight(f: _): Any = macro TupleMacros.foldRight[Tup]
  //def zipMap[Tup2](tup2: Tuple[Tup2]. f: _): Any = macro TupleMacros.zip[Tup]
}

//class Scalar[S](val s: S) {
//
//}

object TupleImplicit {
  implicit def mkTuple[Tup](tup: Tup): Tuple[Tup] = macro TupleMacros.mkTuple[Tup]
}

class InferFunction1[T] {
  def infer[R](f: T => R) = f
}
object InferFunction1 {
  def apply[T] = new InferFunction1[T]
}

class InferFunction2[T1, T2] {
  def infer[R](f: (T1, T2) => R) = f
}
object InferFunction2 {
  def apply[T1, T2] = new InferFunction2[T1, T2]
}

