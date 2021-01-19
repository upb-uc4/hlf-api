package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.PrivateKey
import java.util

import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, PublicExceptionHelper }
import de.upb.cs.uc4.hyperledger.utilities.traits.RegistrationManagerTrait
import org.hyperledger.fabric.gateway.{ Identities, X509Identity }
import org.hyperledger.fabric.sdk.{ Enrollment, User }
import org.hyperledger.fabric_ca.sdk.{ HFCAClient, RegistrationRequest }

object RegistrationManager extends RegistrationManagerTrait {

  @throws[Exception]
  override def register(
      caURL: String,
      caCert: Path,
      userName: String,
      adminName: String,
      adminWalletPath: Path,
      affiliation: String,
      maxEnrollments: Integer = 1,
      newUserType: String = HFCAClient.HFCA_TYPE_CLIENT
  ): String = {
    Logger.debug(s"Begin Registration process. Adding User '$userName' as Admin '$adminName'")
    PublicExceptionHelper.wrapInvocationWithNetworkException[String](
      () => {
        val adminIdentity: X509Identity = WalletManager.getX509Identity(adminWalletPath, adminName)
        val admin: User = RegistrationManager.getUserFromX509Identity(adminIdentity, affiliation)
        val registrationRequest = RegistrationManager.prepareRegistrationRequest(userName, maxEnrollments, newUserType)
        val caClient: HFCAClient = CAClientManager.getCAClient(caURL, caCert)

        // registration process
        try {
          caClient.register(registrationRequest, admin)
        }
        catch {
          case e: Exception => throw Logger.err(s"Registration for the user '$userName' went wrong.", e)
        }
      },
      channel = null,
      chaincode = null,
      networkDescription = null,
      identity = adminName,
      organisationId = null,
      organisationName = affiliation
    )
  }

  // TODO set affiliation?
  private def prepareRegistrationRequest(userName: String, maxEnrollments: Integer = 1, newUserType: String = HFCAClient.HFCA_TYPE_CLIENT): RegistrationRequest = {
    val registrationRequest = new RegistrationRequest(userName)
    registrationRequest.setMaxEnrollments(maxEnrollments)
    registrationRequest.setType(newUserType)
    registrationRequest
  }

  // TODO read affiliation from identity?
  private def getUserFromX509Identity(identity: X509Identity, affiliationName: String): User = {
    val name = getNameFromIdentity(identity)
    new User() {
      override def getName: String = name

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
