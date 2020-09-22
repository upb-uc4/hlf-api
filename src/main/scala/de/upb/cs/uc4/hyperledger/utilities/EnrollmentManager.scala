package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.{KeyPair, KeyPairGenerator}

import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest

object EnrollmentManager {
  /** Register a new User with the CA
    *
    * @param caURL Address to find the CA.
    * @param caCert Certificate to check the validity of the CA.
    * @param walletPath Wallet to store the new user's certificate.
    * @param username Name of the user to be enrolled
    * @param password Password of the user to be enrolled
    * @param organisationId Organisation ID, that the user belongs to.
    * @param csr_pem csr to sign - optional
    * @throws Exception if
    *                   1. The CA Client could not be retrieved from the caURL and Certificate
    *                   2. The enrollment process fails. Maybe your user is not registered?
    */
  def enroll(
      caURL: String,
      caCert: Path,
      walletPath: Path,
      username: String,
      password: String,
      organisationId: String,
      csr_pem: String = null
  ): Unit = {
    // check if user already exists in my wallet
    if (WalletManager.containsIdentity(walletPath, username)) {
      Logger.warn(s"An identity for the user $username already exists in the wallet.")
    }
    else {
      Logger.info(s"Try to get the identity for the user $username.")

      val caClient = CAClientManager.getCAClient(caURL, caCert)

      val enrollmentRequestTLS = EnrollmentManager.prepareEnrollmentRequest("localhost", "tls", csr_pem)
      val enrollment = caClient.enroll(username, password, enrollmentRequestTLS)
      Logger.info("Successfully performed and retrieved enrollment")

      // store in wallet
      val identity = Identities.newX509Identity(organisationId, enrollment)
      Logger.info("Created identity from enrollment")
      WalletManager.putIdentity(walletPath, username, identity)
      Logger.info(s"Successfully enrolled user $username and inserted it into the wallet.")
    }
  }

  private def prepareEnrollmentRequest(host: String, profile: String, csr_pem: String = null): EnrollmentRequest = {
    val enrollmentRequestTLS = new EnrollmentRequest
    enrollmentRequestTLS.addHost(host)
    enrollmentRequestTLS.setProfile(profile)
    enrollmentRequestTLS.setCsr(csr_pem)
    if (csr_pem != null) {
      enrollmentRequestTLS.setKeyPair(generateGarbageKeyPair())
    }
    enrollmentRequestTLS
  }

  private def generateGarbageKeyPair(): KeyPair = {
    KeyPairGenerator.getInstance("RSA").generateKeyPair()
  }
}