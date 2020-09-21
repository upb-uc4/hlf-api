package de.upb.cs.uc4.hyperledger.utilities.helper

import java.nio.file.Path
import java.util.Properties

import de.upb.cs.uc4.hyperledger.utilities.CAClientManager
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.HFCAClient

protected[utilities] object Logger {

  /** Logger utility to encapsulate printing debug messages.
    * Maybe we can have some flag set to enable/disable in the future.
    *
    * @return The HFCAClient object to perform registration/enrollment on.
    */
  def log(message: String): Unit = {
    println(s"[DEBUG] :: $message")
  }

  private def prepareCAClientProperties(tlsCert: Path): Properties = {
    val props = new Properties
    props.put("pemFile", tlsCert.toAbsolutePath.toString)
    props.put("allowAllHostNames", "true")
    props
  }
}
