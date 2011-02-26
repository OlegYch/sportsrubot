package net.scalabot

import javassist.util.proxy.{MethodHandler, ProxyFactory}
import java.lang.reflect.{InvocationTargetException, Method}

/**
 * @author OlegYch
 */

object Delegate {
  def of[T: Manifest](d: => T) = new ProxyFactory {
    val clazz = manifest[T].erasure
    setSuperclass(clazz)
    setHandler(new MethodHandler {
      def invoke(self: AnyRef, thisMethod: Method, proceed: Method, args: Array[AnyRef]) = {
        try {
          thisMethod.invoke(d, args: _*)
        }
        catch {
          case e: InvocationTargetException => throw e.getCause
          case e => throw e
        }
      }
    })
  }.create(Array(), Array()).asInstanceOf[T]
}