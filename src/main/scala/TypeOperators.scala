package macrogen

import scala.language.experimental.macros

import scala.reflect.macros.Context

/** Thanks to Miles Sabin
 *  This is a rewrite in macro style of type operators found in Shapeless
 *  using implicit macro definition feature. This allows searching for
 *  such type operators witnesses from within macros.
 *  Ambiguous implicit definitions work by not compiling but it fails on
 *  macros too, so macros needed some other means.
 */

object TypeOperators {

  // Basic definitions
  type Id[+T] = T
  type Const[C] = {
    type Kind[T] = C
  }

  trait &:&[A, B]

  implicit def andTypeOp[A, B](implicit e1: A, e2: B): A &:& B = new &:&[A, B] {}

  trait !:![A]

  implicit def notTypeOp[A]: macrogen.TypeOperators.!:![A] = macro notTypeOpImpl[A]

  def notTypeOpImpl[A: c.WeakTypeTag](c: Context) = {
    import c.universe._

    if(c.inferImplicitValue(weakTypeOf[A]) == EmptyTree)
      reify(new !:![A] {})
    else
      sys.error("Implicit of type " + weakTypeOf[A] + " exists")
  }
 
  trait |:|[A, B]

  implicit def orTypeOp[A, B]: macrogen.TypeOperators.|:|[A, B] = macro orTypeOpImpl[A, B]

  def orTypeOpImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context): c.Expr[A |:| B] = {
    import c.universe._

    if(c.inferImplicitValue(weakTypeOf[A]) != EmptyTree ||
       c.inferImplicitValue(weakTypeOf[B]) != EmptyTree)
      reify(new |:|[A, B] {})
    else
      sys.error("Not found one implicit of either type " + weakTypeOf[A] + " or " + weakTypeOf[B])
  }


  // Type inequalities
  trait =:!=[A, B] 

  implicit def neqTypeOp[A, B] : macrogen.TypeOperators.=:!=[A, B] = macro neqTypeOpImpl[A, B]

  def neqTypeOpImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context) = {
    import c.universe._

    if(!(weakTypeOf[A] =:= weakTypeOf[B]))
      reify(new =:!=[A, B] {})
    else
      sys.error("Types " + weakTypeOf[A] + " and " + weakTypeOf[B] + " are equal")
  
  }
  
  trait <:!<[A, B]

  implicit def nsubTypeOp[A, B] : macrogen.TypeOperators.<:!<[A, B] = macro nsubTypeOpImpl[A, B]

  def nsubTypeOpImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context) = {
    import c.universe._

    if(!(weakTypeOf[A] <:< weakTypeOf[B]))
      reify(new <:!<[A, B] {})
    else
      sys.error("Type " + weakTypeOf[A] + " is not a subtype of " + weakTypeOf[B])
  
  }

  trait >:!>[A, B]

  implicit def nsubTypeOp2[A, B] : macrogen.TypeOperators.>:!>[A, B] = macro nsubTypeOp2Impl[A, B]

  def nsubTypeOp2Impl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context) = {
    import c.universe._

    if(!(weakTypeOf[B] <:< weakTypeOf[A]))
      reify(new >:!>[A, B] {})
    else
      sys.error("Type " + weakTypeOf[A] + " is not a subtype of " + weakTypeOf[B])
  
  }

  trait <:%<[A, B]

  implicit def nweakSubTypeOp[A, B] : macrogen.TypeOperators.<:%<[A, B] = macro nweakSubTypeOpImpl[A, B]

  def nweakSubTypeOpImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context) = {
    import c.universe._

    if(!(weakTypeOf[A] weak_<:< weakTypeOf[B]))
      reify(new <:%<[A, B] {})
    else
      sys.error("Type " + weakTypeOf[A] + " is not a subtype of " + weakTypeOf[B])
  
  }




}

