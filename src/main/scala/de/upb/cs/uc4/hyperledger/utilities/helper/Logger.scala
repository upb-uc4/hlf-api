package de.upb.cs.uc4.hyperledger.utilities.helper

import java.nio.file.Path
import java.util.Properties

import de.upb.cs.uc4.hyperledger.utilities.CAClientManager
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.HFCAClient

protected[hyperledger] object Logger {

  /** Logger utility to encapsulate printing debug messages.
    * Maybe we can have some flag set to enable/disable in the future.
    *
    * @return The HFCAClient object to perform registration/enrollment on.
    */
  def err(message: String, e: Exception = null): Exception = {
    val msg = s"[ERROR] :: $message"
    println(msg)
    throw new Exception(msg, e)
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
