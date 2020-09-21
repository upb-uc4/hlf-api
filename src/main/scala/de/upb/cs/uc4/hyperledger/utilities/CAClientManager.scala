package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.util.Properties

import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.HFCAClient

protected object CAClientManager {

  /** Retrieves a clientObject for a CA spec.
    * @param caURL Address to find the CA.
    * @param tlsCert Certificate to check the validity of the CA.
    * @return The HFCAClient object to perform registration/enrollment on.
    */
  def getCAClient(caURL: String, tlsCert: Path): HFCAClient = {
    // Create a CA client for interacting with the CA
    val props = CAClientManager.prepareCAClientProperties(tlsCert)
    val caClient = HFCAClient.createNewInstance(caURL, props)

    // set up crypto suite for CAClient
    val cryptoSuite = CryptoSuiteFactory.getDefault.getCryptoSuite
    caClient.setCryptoSuite(cryptoSuite)

    // return
    Logger.log("Created CAClient-Object")
    caClient
  }

  private def prepareCAClientProperties(tlsCert: Path): Properties = {
    val props = new Properties
    props.put("pemFile", tlsCert.toAbsolutePath.toString)
    props.put("allowAllHostNames", "true")
    props
  }
}