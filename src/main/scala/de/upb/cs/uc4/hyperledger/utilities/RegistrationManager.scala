package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.PrivateKey
import java.util

import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.{ Identities, X509Identity }
import org.hyperledger.fabric.sdk.{ Enrollment, User }
import org.hyperledger.fabric_ca.sdk.{ HFCAClient, RegistrationRequest }

object RegistrationManager {

  /** Register a new User with the CA
    *
    * @param caURL Address to find the CA.
    * @param caCert Certificate to check the validity of the CA.
    * @param userName Name of the new user.
    * @param adminName Name of the Admin User you want to access to perform the registration.
    * @param adminWalletPath Wallet containing the admin-certificate.
    * @param affiliation Organisation name, the admin belongs to and the new user will belong to as well.
    * @param maxEnrollments Number of times the user can be enrolled/reenrolled with the username-padssword combination.
    * @param newUserType Permission Level of the new User. Default :: Client.
    * @throws Exception if
    *                   1. The admin user could not be retrieved from the wallet.
    *                   2. The CA Client could not be retrieved from the caURL and Certificate
    *                   3. The registration process fails. Your admin user probably has insufficient permissions.
    * @return the newly created password to use for the user when enrolling.
    */
  @throws[Exception]
  def register(
      caURL: String,
      caCert: Path,
      userName: String,
      adminName: String,
      adminWalletPath: Path,
      affiliation: String,
      maxEnrollments: Integer = 1,
      newUserType: String = HFCAClient.HFCA_TYPE_CLIENT
  ): String = {

    // retrieve Admin Identity as a User
    val adminIdentity: X509Identity = WalletManager.getX509Identity(adminWalletPath, adminName)
    Logger.log(s"AdminIdentity: '${adminIdentity.getCertificate.toString}'")
    val admin: User = RegistrationManager.getUserFromX509Identity(adminIdentity, affiliation)
    Logger.log(s"AdminUser: '${admin.toString}'")

    // prepare registrationRequest
    val registrationRequest = RegistrationManager.prepareRegistrationRequest(userName, maxEnrollments, newUserType)

    // get caClient
    val caClient: HFCAClient = CAClientManager.getCAClient(caURL, caCert)

    // actually perform the registration process
    try {
      caClient.register(registrationRequest, admin)
    }
    catch {
      case e: Exception => throw new Exception(s"Registration for the user '$userName' went wrong.", e)
    }
  }

  /** "If no affiliation is specified in the registration request,
    * the identity being registered will be given the affiliation of the registrar."
    *
    * @param userName
    * @param newUserType
    * @return
    */
  private def prepareRegistrationRequest(userName: String, maxEnrollments: Integer = 1, newUserType: String = HFCAClient.HFCA_TYPE_CLIENT): RegistrationRequest = {
    val registrationRequest = new RegistrationRequest(userName)
    registrationRequest.setMaxEnrollments(maxEnrollments)
    registrationRequest.setType(newUserType)
    registrationRequest
  }

  private def getUserFromX509Identity(identity: X509Identity, affiliationName: String): User = {
    val name = getNameFromIdentity(identity)
    Logger.log("Retrieved Name from identity: '$name'")
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
    val rawName = identity.getCertificate.getSubjectDN.getName
    var name = rawName.substring(rawName.indexOf("=") + 1)
    name = name.substring(0, name.indexOf(","))
    name
  }
}
