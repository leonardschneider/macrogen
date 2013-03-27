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

    val (argsTpes, returnTpes) = weakTypeOf[T] match {
      case TypeRef(_, sym, tpes) if FunctionClass.contains(sym) => (tpes.init, tpes.last)
      case r => (Nil, r)
    }
    val (returnTpe, parents) = returnTpes match {
      case RefinedType(t1 :: ts, _) => (tq"$t1", ts.map(t => tq"$t"))
      case r => (tq"$r", Nil)
    }
    //c.echo(NoPosition, "returnTpe: " + returnTpe)
    //c.echo(NoPosition, "parents: " + parents.mkString(", "))
    val (argsNames, argsDecls) = argsTpes.map(tpe => { 
      val name = TermName(c.fresh("arg$"))
      val term = Ident(name)
      val decl = ValDef(Modifiers(PARAM), name, TypeTree(tpe), EmptyTree)
      (term, decl)
    }).unzip
    val anon1 = 
      if(argsDecls.nonEmpty)
        q"$returnTpe(..$argsNames)"
      else
        q"$returnTpe"
    val anonName = TypeName(c.fresh("anon"))
    val anonDecl = q"class $anonName extends ..${anon1 :: parents}"
    /*val anonDecl = ClassDef(NoMods, anonName, Nil, Template(anon1 :: parents, emptyValDef, 
      List(
        DefDef(
          NoMods,
          nme.CONSTRUCTOR, Nil, List(Nil), TypeTree(), Block(List(pendingSuperCall), Literal(Constant(())))))))
    */
    val ctor =
      if(argsDecls.nonEmpty)
        Function(argsDecls, q"{$anonDecl; new $anonName}")
      else
        q"{$anonDecl; new $anonName}"
    val tree = 
      q"""new macrogen.Constructor.Constructor[${weakTypeOf[T]}] {
          def make = $ctor
        }
      """
    //c.echo(NoPosition, "" + tree)
    //c.echo(NoPosition, showRaw(tree))
    c.Expr[Constructor[T]](c.typeCheck(tree))
  }

  implicit def mkCtor[T]: macrogen.Constructor.Constructor[T] = macro apply[T]
}


