package macrogen

import scala.language.experimental.macros
import scala.reflect.macros.Macro

object TypeOf {
  type TypeOf[T](s: T) = macro TypeOfMacro.TypeOf[T]
}

trait TypeOfMacro extends Macro {
  import c.universe._

  def TypeOf[T: WeakTypeTag](s: Expr[T]): Tree = {
    tq"${weakTypeOf[T]}"
  }
}

