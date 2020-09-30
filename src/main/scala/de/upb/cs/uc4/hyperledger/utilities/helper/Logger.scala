package de.upb.cs.uc4.hyperledger.utilities.helper

protected[hyperledger] object Logger {

  /** Logger utility to encapsulate printing debug messages.
    * Maybe we can have some flag set to enable/disable in the future.
    *
    * @return The HFCAClient object to perform registration/enrollment on.
    */
  def err(message: String, e: Exception = null): Exception = {
    val msg = s"[ERROR] :: $message \nMessage :: ${e.getMessage}\nStackTrace :: ${e.getStackTrace.mkString("Array(", ", ", ")")}"
    println(msg)
    new Exception(msg, e)
  }

  /** Logger utility to encapsulate printing debug messages.
    * Maybe we can have some flag set to enable/disable in the future.
    *
    * @return The HFCAClient object to perform registration/enrollment on.
    */
  def warn(message: String): Unit = {
    println(s"[WARNING] :: $message")
  }

  /** Logger utility to encapsulate printing debug messages.
    * Maybe we can have some flag set to enable/disable in the future.
    *
    * @return The HFCAClient object to perform registration/enrollment on.
    */
  def debug(message: String): Unit = {
    println(s"[DEBUG] :: $message")
  }

  /** Logger utility to encapsulate printing debug messages.
    * Maybe we can have some flag set to enable/disable in the future.
    *
    * @return The HFCAClient object to perform registration/enrollment on.
    */
  def info(message: String): Unit = {
    println(s"[INFO] :: $message")
  }
}
