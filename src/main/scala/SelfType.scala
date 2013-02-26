package macrogen

import language.experimental.macros
import scala.reflect.macros.Context

object SelfType {
  
  def selfTypeImpl(c: Context): c.Tree = {
    import c.universe._

    val Template(parents, self, defs) = c.enclosingTemplate
    val tpe = Ident(c.enclosingImpl.name)
    val typedef = q"type Self = $tpe"
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

