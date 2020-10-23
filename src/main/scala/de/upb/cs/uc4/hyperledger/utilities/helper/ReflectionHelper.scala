package de.upb.cs.uc4.hyperledger.utilities.helper

import java.lang.reflect.{ Field, Method }

protected[hyperledger] object ReflectionHelper {

  def callPrivateMethod(instance: AnyRef)(methodName: String)(args: AnyRef*): AnyRef = {
    def _parents: LazyList[Class[_]] = LazyList(instance.getClass) #::: _parents.map(_.getSuperclass)
    val parents: List[Class[_]] = _parents.takeWhile(_ != null).toList
    val methods: List[Method] = parents.flatMap(_.getDeclaredMethods)
    val method: Method = methods
      .find(method => method.getName == methodName && method.getParameterCount == args.length)
      .getOrElse(throw new IllegalArgumentException("Method " + methodName + " not found"))
    method.setAccessible(true)
    try {
      method.invoke(instance, args: _*)
    }
    catch {
      case ex: Throwable => throw Logger.err("Inner Exception on private method call: ", ex)
    }
  }

  def setPrivateField(instance: AnyRef)(fieldName: String)(arg: AnyRef): Unit = {
    def _parents: LazyList[Class[_]] = LazyList(instance.getClass) #::: _parents.map(_.getSuperclass)
    val parents: List[Class[_]] = _parents.takeWhile(_ != null).toList
    val fields: List[Field] = parents.flatMap(_.getDeclaredFields)
    val field: Field = fields
      .find(_.getName == fieldName)
      .getOrElse(throw new IllegalArgumentException("Method " + fieldName + " not found"))
    field.setAccessible(true)
    field.set(instance, arg)
  }
}
