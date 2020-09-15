package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.util.Properties

import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.{ EnrollmentRequest, HFCAClient }

object CAManager {
  def getCAClient(caURL: String, tlsCert: Path): HFCAClient = {
    // Create a CA client for interacting with the CA
    val props = new Properties
    props.put("pemFile", tlsCert.toAbsolutePath.toString)
    props.put("allowAllHostNames", "true")
    val caClient = HFCAClient.createNewInstance(caURL, props)
    val cryptoSuite = CryptoSuiteFactory.getDefault.getCryptoSuite
    caClient.setCryptoSuite(cryptoSuite)
    println(s"Created cryptoSuite.")

    caClient
  }
}