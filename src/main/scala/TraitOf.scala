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

    //c.echo(NoPosition, "Enclosing unit\n" +
    //  c.enclosingUnit.body.collect {
    //    case x @ ClassDef(_, name, _, impl) if name == weakTypeOf[T].typeSymbol.name => impl.body
    //  }.head.map(t => showRaw(t)).mkString("\n"))

    val impldefs = c.enclosingUnit.body.collect {
        case x @ ClassDef(_, name, _, impl) if name == weakTypeOf[T].typeSymbol.name => impl.body
    }.head
    def isPublic(mods: Modifiers) = !mods.hasFlag(PRIVATE) && !mods.hasFlag(PROTECTED)
    val srcdefs = impldefs.flatMap(d => d match {
      case ValDef(mods, name, tpt, rhs) if isPublic(mods) =>
        List(ValDef(Modifiers(mods.flags | DEFERRED), name, tpt, EmptyTree))
      case DefDef(mods, name, tparams, vparamss, tpt, rhs) if isPublic(mods) =>
        List(DefDef(Modifiers(mods.flags | DEFERRED), name, tparams, vparamss, tpt, EmptyTree))
      case TypeDef(mods, name, tparams, bounds @ TypeBoundsTree(_, _)) if isPublic(mods) =>
        List(TypeDef(Modifiers(mods.flags | DEFERRED), name, tparams, bounds))
      case ClassDef(mods, name, tparams, impl) if isPublic(mods) =>
        List(ClassDef(mods, name, tparams, impl))
      case ModuleDef(mods, name, impl) if isPublic(mods) =>
        List(ModuleDef(mods, name, impl))
      case _ => Nil
    })

    //c.echo(NoPosition, "srcdefs\n" + srcdefs.mkString("\n"))

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

