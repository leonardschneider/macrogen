
package macrogen

import language.experimental.macros
import scala.reflect.macros.Context

object SelfType {
  
  def selfTypeImpl(c: Context): c.Tree = {
    import c.universe._

    val Template(parents, self, defs) = c.enclosingTemplate
    //c.echo(NoPosition, "Parents " + parents.map(t => showRaw(t)))
    //c.echo(NoPosition, "Filtered parents " + 
    //  parents.filter(t => !t.equalsStructure(Ident(TypeName("SelfType")))))
    val tpe = Ident(c.enclosingImpl.name)
    //c.echo(NoPosition, "Self " + tpe)
    val typedef = q"type Self = $tpe"
    val res = Template(
      parents.filter(t => !t.equalsStructure(Ident(TypeName("SelfType")))),
      self,
      typedef +: defs
    )
    //c.echo(NoPosition, "Generating type with Self type" + res)
    res
  }

  type SelfType = macro selfTypeImpl

}

