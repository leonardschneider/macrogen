package macrogen

import language.experimental.macros
import scala.reflect.macros.{Context, Macro}

object Copiable {

  def copiableImpl(c: Context): c.Tree = {
    import c.universe._
    import Flag._

    val Template(parents, self0, defs) = c.enclosingTemplate

    //c.echo(NoPosition, "" + showRaw(self0) + (self0 == emptyValDef))
    c.echo(NoPosition, "Parents " + parents.map(_.symbol))

    val self = 
      if(self0 == emptyValDef) {
        val name = c.freshName("self$")
        // could not find a way to express this with quasiquotes
        ValDef(Modifiers(Flag.PRIVATE), TermName(name), TypeTree(), EmptyTree)
      }
      else
        self0

    val tpeName = c.enclosingImpl.name

    val selfName = self.name

    val inits = defs.flatMap(d => d match {
      case ValDef(mods, name, tpt, rhs) if mods.hasFlag(MUTABLE) =>
        List(q"$name = $selfName.$name")
      case ValDef(mods0, name, tpt, rhs) =>
        val mods = 
          if(rhs != EmptyTree)
            Modifiers(OVERRIDE)
          else
            NoMods
        List(ValDef(mods, name, tpt, q"$selfName.$name"))
      case DefDef(mods0, name, tparams, vparamss0, tpt, rhs)
        if(name != nme.CONSTRUCTOR && name != TermName("$init$")) => // seems like a CONSTRUCTOR bug
        c.echo(NoPosition, "DefDef " + name + (name == TermName("$init$")))
        val mods = 
          if(rhs.nonEmpty)
            Modifiers(OVERRIDE)
          else
            NoMods
        val vparamss = vparamss0.map(_.map(p => q"${p.name}"))
        c.echo(NoPosition, "vparamss " + vparamss)
        val res = if(tparams == Nil)
          List(DefDef(mods, name, tparams, vparamss0, tpt, 
            q"$selfName.$name(...$vparamss)"))
        else
          List(DefDef(mods, name, tparams, vparamss0, tpt,
            q"$selfName.$name[..$tparams](...$vparamss)"))
        c.echo(NoPosition, "Defdef res " + res)
        res
      case _ => Nil
    })

    val parentsCopy: Option[Tree] = //None /*(c.enclosingImpl.symbol.asClass.baseClasses ++
        parents.map(_.symbol).distinct.filter(_ != NoSymbol).
        filter(
          _.asType.toType.members.
          find(s => s.name == TypeName("Copy") && s.isClass && s.asClass.isTrait).isDefined
        ).
        map(s => Select(Super(This(tpnme.EMPTY), s.name.toTypeName), TypeName("Copy"))).
        reduceLeftOption((t1: Tree, t2: Tree) => tq"$t1 with $t2")

    c.echo(NoPosition, "Parents.copy " + parents.map(_.symbol).distinct.filter(_ != NoSymbol).
      filter(
        _.asType.toType.members.
        find(s => s.name == TypeName("Copy") && s.isClass && s.asClass.isTrait).isDefined
      ).
      map(s => Super(This(tpnme.EMPTY), s.name.toTypeName)).
      reduceLeftOption((t1: Tree, t2: Tree) => tq"$t1 with $t2")
    )
    //  map(s => s.asClass.baseClasses)
    //)

    val copyClazz = 
      if(parentsCopy.isEmpty)
        q"trait Copy extends $tpeName {..$inits}"
      else
      //  q"trait Copy extends $tpeName {..$inits}"
        q"trait Copy extends $tpeName with ${parentsCopy.get} {..$inits}"

    val copyDef =
      if(parentsCopy.isEmpty)
        q"def copy: $tpeName = new Copy {} "
      else
        q"override def copy: $tpeName = new Copy {} "

    val res = Template(
      parents.filter(t => !t.equalsStructure(tq"Copiable")), // :+ Ident("CopyConstructor": TypeName),
      self,
      defs :+ copyClazz :+ copyDef)

    c.echo(NoPosition, "res " + res)

    res
  
  }

  type Copiable = macro copiableImpl

}

