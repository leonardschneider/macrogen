package macrogen

import language.experimental.macros
import scala.reflect.macros.Context

object SelfType {
  
  def selfTypeImpl(c: Context): c.Tree = {
    import c.universe._

    val Template(parents, self, defs) = c.enclosingTemplate
    val typedef = c.enclosingImpl match {
      case ClassDef(_, name, tparams, _) if tparams.length > 0 =>
        val targs = tparams.map(t => tq"${t.name}")
        q"type Self[..$tparams] = $name[..$targs]"
      case ClassDef(_, name, _, _) =>
        q"type Self = $name"
      case ModuleDef(_, name, _) =>
        q"type Self = $name"
    }
    val res = Template(
      parents.filter(t => !t.equalsStructure(tq"SelfType")),
      self,
      typedef +: defs
    )
    //c.echo(NoPosition, "Generating type with Self type" + res)
    res
  }

  type SelfType = macro selfTypeImpl

}

