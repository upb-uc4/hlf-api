package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.util.Properties

import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.{ EnrollmentRequest, HFCAClient }

object EnrollmentManager {
  def enroll(caURL: String, tlsCert: Path, walletPath: Path, username: String, password: String, organisationId: String): Unit = {
    // wallet is target for admin certificate
    val wallet = WalletManager.getWallet(walletPath)

    // check if user already exists in my wallet
    if (WalletManager.containsIdentity(wallet, username)) {
      println(s"[DEBUG] :: An identity for the user $username already exists in the wallet.")
    }
    else {
      println(s"[DEBUG] :: Try to get the identity for the user $username.")

      val caClient = CAClientManager.getCAClient(caURL, tlsCert)
      println("[DEBUG] :: Retrieved CAClient")

      val enrollmentRequestTLS = EnrollmentManager.prepareEnrollmentRequest("localhost", "tls")
      val enrollment = caClient.enroll(username, password, enrollmentRequestTLS)
      println("[DEBUG] :: retrieved enrollment")

      // store in wallet
      val identity = Identities.newX509Identity(organisationId, enrollment)
      println("[DEBUG] :: created identity from enrollment")
      WalletManager.putIdentity(wallet, username, identity)
      println(s"[DEBUG] :: Successfully enrolled user $username and inserted it into the wallet.")
    }
  }

  private def prepareEnrollmentRequest(host: String, profile: String): EnrollmentRequest = {
    val enrollmentRequestTLS = new EnrollmentRequest
    enrollmentRequestTLS.addHost(host)
    enrollmentRequestTLS.setProfile(profile)
    enrollmentRequestTLS
  }
}