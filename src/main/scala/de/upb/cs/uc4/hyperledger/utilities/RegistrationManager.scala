package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.PrivateKey
import java.util

import org.hyperledger.fabric.gateway.{ Identities, X509Identity }
import org.hyperledger.fabric.sdk.{ Enrollment, User }
import org.hyperledger.fabric_ca.sdk.{ HFCAClient, RegistrationRequest }

object RegistrationManager {

  @throws[Exception]
  def register(tlsCert: Path, caURL: String, userName: String, adminName: String, adminWalletPath: Path, affiliation: String): String = {

    // retrieve Admin Identity as a User
    val wallet = WalletManager.getWallet(adminWalletPath)
    val adminIdentity: X509Identity = WalletManager.getX509Identity(wallet, adminName)
    println("[DEBUG] :: AdminIdentity: " + adminIdentity.getCertificate.toString)
    val admin: User = RegistrationManager.getUserFromX509Identity(adminIdentity, affiliation)
    println("[DEBUG] :: AdminUser: " + admin.toString)

    // prepare registrationRequest
    val registrationRequest = RegistrationManager.prepareRegistrationRequest(userName)

    // get caClient
    val caClient: HFCAClient = CAClientManager.getCAClient(caURL, tlsCert)

    // actually perform the registration process
    try {
      caClient.register(registrationRequest, admin)
    }
    catch {
      case e: Exception => throw new Exception(s"Registration for the user '${userName}' went wrong.", e)
    }
  }

  /** "If no affiliation is specified in the registration request,
    * the identity being registered will be given the affiliation of the registrar."
    *
    * @param userName
    * @param newUserType
    * @return
    */
  private def prepareRegistrationRequest(userName: String, newUserType: String = HFCAClient.HFCA_TYPE_CLIENT): RegistrationRequest = {
    val registrationRequest = new RegistrationRequest(userName)
    registrationRequest.setType(newUserType)
    registrationRequest
  }

  private def getUserFromX509Identity(identity: X509Identity, affiliationName: String): User = {
    val name = getNameFromIdentity(identity)
    new User() {
      override def getName = name
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

  private def getNameFromIdentity(identity: X509Identity): String = {
    // println("[DEBUG] ::  TEST: " + identity.getCertificate.getSubjectDN.getName)
    // CN=scala-registration-admin-org1, OU=admin

    // println("[DEBUG] ::  TEST: " + identity.getCertificate.getSigAlgName)
    // SHA256withECDSA

    // println("[DEBUG] ::  TEST: " + identity.getCertificate.getSigAlgOID)
    // 1.2.840.10045.4.3.2

    // println("[DEBUG] ::  TEST: " + identity.getCertificate.getSigAlgParams)
    // null

    // println("[DEBUG] ::  TEST: " + identity.getCertificate.getSignature)
    // [B@5d6f35bf

    // identity.getCertificate.getSubjectAlternativeNames.forEach(a => println("[DEBUG] ::  TEST: " + a))
    // [2, localhost]

    val rawName = identity.getCertificate.getSubjectDN.getName
    var name = rawName.substring(rawName.indexOf("=") + 1)
    name = name.substring(0, name.indexOf(","))
    println("[DEBUG] ::  name: " + name)
    name
  }
}
