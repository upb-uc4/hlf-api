package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.{ Path }
import java.security.PrivateKey
import java.util

import org.hyperledger.fabric.gateway.{ Identities, Wallets, X509Identity }
import org.hyperledger.fabric.sdk.{ Enrollment, User }
import org.hyperledger.fabric_ca.sdk.{ RegistrationRequest }

object RegistrationManager {

  @throws[Exception]
  def register(tlsCert: Path, caURL: String, userName: String, adminName: String, adminWalletPath: Path, organisationId: String): String = {
    val wallet = WalletManager.getWallet(adminWalletPath)

    val adminExists = wallet.list.contains(adminName)
    if (!adminExists) {
      throw new Exception(s"'${adminName}' needs to be enrolled and added to the wallet first")
    }

    var admin: User = null
    try {
      val adminIdentity: X509Identity = wallet.get(adminName).asInstanceOf[X509Identity]
      admin = new User() {
        override def getName = adminName
        override def getRoles: util.Set[String] = null
        override def getAccount = ""
        override def getAffiliation: String = organisationId
        override def getEnrollment: Enrollment = new Enrollment() {
          override def getKey: PrivateKey = adminIdentity.getPrivateKey
          override def getCert: String = Identities.toPemString(adminIdentity.getCertificate)
        }
        override def getMspId: String = organisationId
      }
      println(adminIdentity.getCertificate.toString)
    }
    catch {
      case e: Exception => throw new Exception(s"Could not create an X509Identity for the admin user '${adminName}'", e)
    }

    try {
      println("Create a CA client for interacting with the CA")
      val caClient = CAManager.getCAClient(caURL, tlsCert)
      println("Retrieved CAClient")

      println("Register the user, enroll the user, and import the new identity into the wallet.")
      val registrationRequest = new RegistrationRequest(userName)
      registrationRequest.setAffiliation(organisationId)
      val enrollmentSecret = caClient.register(registrationRequest, admin)
      enrollmentSecret
    }
    catch {
      case e: Exception => throw new Exception(s"Registration for the user '${userName}' went wrong.", e)
    }
  }
}
