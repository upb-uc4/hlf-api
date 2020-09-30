package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.{ KeyPair, KeyPairGenerator }

import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionCertificate
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, PublicExceptionHelper }
import de.upb.cs.uc4.hyperledger.utilities.traits.EnrollmentManagerTrait
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest

object EnrollmentManager extends EnrollmentManagerTrait {

  override def enrollSecure(
      caURL: String,
      caCert: Path,
      enrollmentID: String,
      enrollmentSecret: String,
      csr_pem: String = null,
      adminName: String,
      adminWalletPath: Path,
      channel: String,
      chaincode: String,
      networkDescriptionPath: Path
  ): String = {
    PublicExceptionHelper.wrapInvocationWithNetworkException[String](
      () => {
        Logger.info(s"Try to sign the certificate for the user $enrollmentID.")
        val caClient = CAClientManager.getCAClient(caURL, caCert)
        Logger.info("Successfully created a communication channel with the CA.")
        val enrollmentRequestTLS = EnrollmentManager.prepareEnrollmentRequest(enrollmentID, "tls", csr_pem)
        Logger.info("Successfully prepared the enrollmentRequest.")
        val enrollment = caClient.enroll(enrollmentID, enrollmentSecret, enrollmentRequestTLS)
        Logger.info("Successfully performed and retrieved enrollment.")
        val certificate = enrollment.getCert
        Logger.info("Retrieved SignedCertificate.")

        // store cert on chaincode
        val certificateConnection = ConnectionCertificate(adminName, channel, chaincode, adminWalletPath, networkDescriptionPath)
        certificateConnection.addCertificate(enrollmentID, certificate)
        Logger.info("Successfully stored cert on new chaincode")

        certificate
      },
      channel,
      chaincode,
      networkDescriptionPath.toString,
      adminName
    )
  }

  override def enroll(
      caURL: String,
      caCert: Path,
      walletPath: Path,
      enrollmentID: String,
      enrollmentSecret: String,
      organisationId: String,
      channel: String,
      chaincode: String,
      networkDescriptionPath: Path
  ): Unit = {
    PublicExceptionHelper.wrapInvocationWithNetworkException(
      () => {
        // check if user already exists in my wallet
        if (WalletManager.containsIdentity(walletPath, enrollmentID)) {
          Logger.warn(s"An identity for the user $enrollmentID already exists in the wallet.")
        }
        else {
          Logger.info(s"Try to get the identity for the user $enrollmentID.")

          val caClient = CAClientManager.getCAClient(caURL, caCert)

          val enrollmentRequestTLS = EnrollmentManager.prepareEnrollmentRequest(enrollmentID, "tls")
          val enrollment = caClient.enroll(enrollmentID, enrollmentSecret, enrollmentRequestTLS)
          Logger.info("Successfully performed and retrieved enrollment")

          // store in wallet
          val identity = Identities.newX509Identity(organisationId, enrollment)
          Logger.info("Created identity from enrollment.")
          WalletManager.putIdentity(walletPath, enrollmentID, identity)
          Logger.info(s"Successfully enrolled user $enrollmentID and inserted it into the wallet.")

          // store cert on chaincode
          val certificateConnection = ConnectionCertificate(enrollmentID, channel, chaincode, walletPath, networkDescriptionPath)
          certificateConnection.addCertificate(enrollmentID, enrollment.getCert)
          Logger.info("Successfully stored cert on new chaincode")
        }
      },
      channel,
      chaincode,
      networkDescriptionPath.toString,
      enrollmentID,
      organisationId
    )
  }

  private def prepareEnrollmentRequest(
      hostName: String,
      profile: String,
      csr_pem: String = null
  ): EnrollmentRequest = {
    val enrollmentRequestTLS = new EnrollmentRequest
    enrollmentRequestTLS.addHost(hostName)
    enrollmentRequestTLS.setProfile(profile)
    if (csr_pem != null) {
      enrollmentRequestTLS.setCsr(csr_pem)
      enrollmentRequestTLS.setKeyPair(generateGarbageKeyPair())
    }
    enrollmentRequestTLS
  }

  private def generateGarbageKeyPair(): KeyPair = {
    KeyPairGenerator.getInstance("RSA").generateKeyPair()
  }
}