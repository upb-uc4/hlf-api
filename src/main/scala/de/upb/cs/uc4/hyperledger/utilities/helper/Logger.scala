package de.upb.cs.uc4.hyperledger.utilities.helper

protected[hyperledger] object Logger {

  /** Logger utility to encapsulate printing error messages.
    * Maybe we can have some flag set to enable/disable in the future.
    *
    * @return A new Exception encapsulating the error.
    */
  def err(message: String, exception: Throwable = null): Throwable = {
    val msg = s"""
              [MESSAGE] :: $message
              [ERROR] :: ${getInfoFromThrowable(exception)}
              """
    println(msg)
    exception
  }

  /** Logger utility to encapsulate printing warning messages.
    * Maybe we can have some flag set to enable/disable in the future.
    */
  def warn(message: String): Unit = {
    println(s"[WARNING] :: $message")
  }

  /** Logger utility to encapsulate printing debug messages.
    * Maybe we can have some flag set to enable/disable in the future.
    */
  def debug(message: String): Unit = {
    println(s"[DEBUG] :: $message")
  }

  /** Logger utility to encapsulate printing info messages.
    * Maybe we can have some flag set to enable/disable in the future.
    */
  def info(message: String): Unit = {
    println(s"[INFO] :: $message")
  }

  def getInfoFromThrowable(exception: Throwable): String = {
    if (exception != null) {
      s"""
        Exception-Message :: ${exception.getMessage}
        Exception-StackTrace :: ${exception.getStackTrace.mkString("Array(", ", ", ")")}
        Exception-Inner :: ${getInfoFromThrowable(exception.getCause)}
      """
    }
    else {
      ""
    }
  }
}
