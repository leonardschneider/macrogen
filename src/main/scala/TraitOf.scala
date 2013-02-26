package macrogen

import language.experimental.macros
import scala.reflect.macros.Context

object TraitOf {

  def TraitOfImpl[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    import Flag._

    val Template(parents, self, defs) = c.enclosingTemplate

    //c.echo(NoPosition, "Class defs " + defs)

    // Terms: val, var, def
    val decls = weakTypeOf[T].declarations.
      filter(s => s.name != nme.CONSTRUCTOR && s.isMethod && s.isPublic).
      map(_.asMethod).
      map(s => DefDef(s, EmptyTree)).toList.flatMap(d => d match {
        case DefDef(mods, name, tparams, vparamss, tpt, rhs) =>
          List(DefDef(Modifiers(mods.flags | DEFERRED), name, tparams, vparamss, tpt, EmptyTree))
        case _ => Nil
      })

    //c.echo(NoPosition, "" + decls)

    val temp = Template(
      parents.filter(t => t match {
        case tq"TraitOf[$t]" => false
        case _ => /*c.echo(NoPosition, "" + t);*/ true
      }),
      self,
      defs ++ decls
    )

    //c.echo(NoPosition, "Generating template " + temp)

    temp

  }

  type TraitOf[T] = macro TraitOfImpl[T]

}

