package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.util.Properties

import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.{EnrollmentRequest, HFCAClient}

object EnrollmentManager{
  def enroll(ca_url : String, tlsCert : Path, walletPath : Path, username : String, password : String, organisationId : String): Unit = {
    // wallet is target for admin certificate
    val wallet = WalletManager.getWallet(walletPath)

    // check if user already exists in my wallet
    if (wallet.list.contains(username)) {
      System.out.println(s"An identity for the admin user $username already exists in the wallet")
      return
    }

    // Create a CA client for interacting with the CA
    val props = new Properties
    props.put("pemFile", tlsCert.toAbsolutePath.toString)
    props.put("allowAllHostNames", "true")
    val caClient = HFCAClient.createNewInstance(ca_url, props)
    val cryptoSuite = CryptoSuiteFactory.getDefault.getCryptoSuite
    caClient.setCryptoSuite(cryptoSuite)

    // enroll my user
    val enrollmentRequestTLS = new EnrollmentRequest
    enrollmentRequestTLS.addHost("localhost")
    enrollmentRequestTLS.setProfile("tls")
    val enrollment = caClient.enroll(username, password, enrollmentRequestTLS)
    val identity = Identities.newX509Identity(organisationId, enrollment)
    wallet.put(username, identity)
    System.out.println(s"Successfully enrolled user ${username} and imported it into the wallet")
  }

}