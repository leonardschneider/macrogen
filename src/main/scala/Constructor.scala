package macrogen

import scala.language.experimental.macros
import scala.reflect.macros.Context

object Constructor {
  trait Constructor[T] {
    def make: T
  }
  
  def apply[T: c.WeakTypeTag](c: Context): c.Expr[Constructor[T]] = {
    import c.universe._
    import Flag._
    import definitions._

    val (argsTpes, returnTpe) = weakTypeOf[T] match {
      case TypeRef(_, sym, tpes) if FunctionClass.contains(sym) => (tpes.init, tpes.last)
      case r => (Nil, r)
    }
    val (argsNames, argsDecls) = argsTpes.map(tpe => { 
      val name = TermName(c.fresh("arg$"))
      val term = Ident(name)
      val decl = ValDef(Modifiers(PARAM), name, TypeTree(tpe), EmptyTree)
      (term, decl)
    }).unzip
    val ctor =
      if(argsDecls.nonEmpty)
        Function(argsDecls, q"new ${returnTpe.typeSymbol}(..$argsNames)")
      else
        q"new ${returnTpe.typeSymbol}"
    val tree = 
      q"""new macrogen.Constructor.Constructor[${weakTypeOf[T]}] {
          def make = $ctor
        }
      """
    //c.echo(NoPosition, "" + tree)
    c.Expr[Constructor[T]](c.typeCheck(tree))
  }

  implicit def mkCtor[T]: macrogen.Constructor.Constructor[T] = macro apply[T]
}


