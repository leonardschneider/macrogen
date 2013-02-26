package macrogen

import scala.language.experimental.macros
import scala.reflect.macros.Macro
import scala.collection.mutable.Map

trait CachedMacro extends Macro {
  import c.universe._

  def cache[B: WeakTypeTag](body: Expr[B]): Expr[B] = {
  
    val DefDef(_, fun, _, argss, _, _) = c.enclosingDef
    def mkArgsTuple(args0: List[ValDef]): Tree = {
      val tup = definitions.TupleClass(args0.length).name.toTermName
      val args = args0.map(v => q"${v.name}")
      val tree = c.typeCheck(q"$tup(..$args)")
      tree
    }
    val argssTuple = {
      val tup = definitions.TupleClass(argss.length).name.toTermName
      val args = argss.map(l => mkArgsTuple(l))
      val tree = c.typeCheck(q"$tup(..$args)")
      c.Expr(tree)(c.WeakTypeTag(tree.tpe))
    }
    val key = reify((c.literal(fun.decoded).splice, argssTuple.splice))

    reify {
      macrogen.cached.defaultMap.getOrElse(key.splice, {
        val res = body.splice
        macrogen.cached.defaultMap += key.splice -> res
        res
      }).asInstanceOf[B]
    }

  }

}

object cached {
  var defaultMap: Map[Any, Any] = Map.empty

  def apply[B](body: B): B = macro CachedMacro.cache[B]

}
