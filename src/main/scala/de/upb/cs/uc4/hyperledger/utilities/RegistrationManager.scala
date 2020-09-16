package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.PrivateKey
import java.util

import org.hyperledger.fabric.gateway.{Identities, X509Identity}
import org.hyperledger.fabric.sdk.{Enrollment, User}
import org.hyperledger.fabric_ca.sdk.{HFCAClient, RegistrationRequest}

object RegistrationManager {

  @throws[Exception]
  def register(tlsCert: Path, caURL: String, userName: String, adminName: String, adminWalletPath: Path, affiliation: String): String = {
    // retrieve Admin Identity ad a User
    val wallet = WalletManager.getWallet(adminWalletPath)
    val adminIdentity: X509Identity = WalletManager.getX509Identity(wallet, adminName)
    println("[DEBUG] AdminIdentity: " + adminIdentity.getCertificate.toString)
    val admin: User = RegistrationManager.getUserFromX509Identity(adminIdentity, affiliation)
    println("[DEBUG] AdminUser: " + admin.toString)

    // prepare registrationRequest
    val registrationRequest = RegistrationManager.prepareRegistrationRequest(userName, affiliation)

    // get caClient
    val caClient: HFCAClient = CAManager.getCAClient(caURL, tlsCert)

    // actually perform the registration process
    try {
      caClient.register(registrationRequest, admin)
    }
    catch {
      case e: Exception => throw new Exception(s"Registration for the user '${userName}' went wrong.", e)
    }
  }

  private def prepareRegistrationRequest(userName: String, affiliation: String, newUserType: String = HFCAClient.HFCA_TYPE_CLIENT): RegistrationRequest = {
    val registrationRequest = new RegistrationRequest(userName, affiliation)
    registrationRequest.setType(newUserType)
    registrationRequest
  }

  private def getUserFromX509Identity(identity : X509Identity, affiliationName: String): User = {
    println("AAAAA TEST: "+identity.getCertificate.getSubjectDN.getName )
    println("AAAAA TEST: "+identity.getCertificate.getSubjectUniqueID.toString )
    println("AAAAA TEST: "+identity.getCertificate.getSigAlgName)
    println("AAAAA TEST: "+identity.getCertificate.getSigAlgOID)
    println("AAAAA TEST: "+identity.getCertificate.getSigAlgParams.toString)
    println("AAAAA TEST: "+identity.getCertificate.getSignature.toString)

    new User() {
      override def getName = identity.getCertificate.getSubjectDN.getName
      override def getRoles: util.Set[String] = null
      override def getAccount = ""
      override def getAffiliation: String = affiliationName
      override def getEnrollment: Enrollment = new Enrollment() {
        override def getKey: PrivateKey = identity.getPrivateKey
        override def getCert: String = Identities.toPemString(identity.getCertificate)
      }
      override def getMspId: String = identity.getMspId
    }
  }
}
