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
    var certificate: String = ""
    PublicExceptionHelper.wrapInvocationWithNetworkException(
      () => {
        Logger.info(s"Try to sign the certificate for the user $enrollmentID.")
        val caClient = CAClientManager.getCAClient(caURL, caCert)
        Logger.info("Successfully created a communication channel with the CA.")
        val enrollmentRequestTLS = EnrollmentManager.prepareEnrollmentRequest(enrollmentID, "tls", csr_pem)
        Logger.info("Successfully prepared the enrollmentRequest.")
        val enrollment = caClient.enroll(enrollmentID, enrollmentSecret, enrollmentRequestTLS)
        Logger.info("Successfully performed and retrieved enrollment.")
        certificate = enrollment.getCert
        Logger.info("Retrieved SignedCertificate.")
      },
      channel,
      chaincode,
      networkDescriptionPath.toString,
      adminName
    )

    putNewCertificateOnChain(adminName, channel, chaincode, adminWalletPath, networkDescriptionPath, enrollmentID, certificate)

    certificate
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
  ): String = {
    // return certificate
    var certificate: String = ""

    // check if user already exists in my wallet
    if (WalletManager.containsIdentity(walletPath, enrollmentID)) {
      Logger.warn(
        s""""
           An identity for the user '$enrollmentID' already exists in your wallet.
           If you want to re-enroll, please delete the current identity file
         """"
      )
      certificate = WalletManager.getX509Identity(walletPath, enrollmentID).getCertificate.toString
    }
    else {
      Logger.info(s"Try to enroll user: '$enrollmentID'.")
      PublicExceptionHelper.wrapInvocationWithNetworkException(
        () => {
          val caClient = CAClientManager.getCAClient(caURL, caCert)

          val enrollmentRequestTLS = EnrollmentManager.prepareEnrollmentRequest(enrollmentID, "tls")
          val enrollment = caClient.enroll(enrollmentID, enrollmentSecret, enrollmentRequestTLS) // throw exception for abortive enrollment
          certificate = enrollment.getCert
          Logger.info("Successfully performed and retrieved enrollment")

          // store in wallet
          val identity = Identities.newX509Identity(organisationId, enrollment)
          Logger.info("Created identity from enrollment.")
          WalletManager.putIdentity(walletPath, enrollmentID, identity)
          Logger.info(s"Successfully enrolled user $enrollmentID and inserted it into the wallet.")
        },
        channel,
        chaincode,
        networkDescriptionPath.toString,
        enrollmentID,
        organisationId
      )
      putNewCertificateOnChain(enrollmentID, channel, chaincode, walletPath, networkDescriptionPath, enrollmentID, certificate)
    }

    certificate
  }

  private def putNewCertificateOnChain(connectionName: String, channel: String, chaincode: String, connectionWalletPath: Path, networkDescriptionPath: Path,
      newEnrollmentID: String, newCertificate: String): Unit = {
    // store certificate on chaincode
    val certificateConnection = ConnectionCertificate(connectionName, channel, chaincode, connectionWalletPath, networkDescriptionPath)
    certificateConnection.addOrUpdateCertificate(newEnrollmentID, newCertificate)
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