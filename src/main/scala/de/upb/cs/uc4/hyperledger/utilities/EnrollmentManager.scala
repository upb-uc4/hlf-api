package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.{KeyPair, KeyPairGenerator}

import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionCertificate
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest

object EnrollmentManager {

  /** Enrolls a new User and stores the X509Identity (KeyPair, SignedCert, MetaData) in the Wallet.
    *
    * @param caURL            Address to find the CA.
    * @param caCert           Certificate to check the validity of the CA.
    * @param enrollmentID     enrollmentID of the user to be enrolled.
    * @param enrollmentSecret Password of the user to be enrolled.
    * @throws Exception       if
    *                         1. The CA Client could not be retrieved from the caURL and Certificate.
    *                         2. The enrollment process fails. Maybe your user is not registered?
    * @return                 the Signed Certificate for the CSR.
    */
  def enrollSecure(
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
    Logger.info(s"Try to sign the certificate for the user $enrollmentID.")
    val caClient = CAClientManager.getCAClient(caURL, caCert)
    Logger.info("Successfully created a communication channel with the CA.")
    val enrollmentRequestTLS = EnrollmentManager.prepareEnrollmentRequest(enrollmentID, "tls", csr_pem)
    Logger.info("Successfully prepared the enrollmentRequest.")
    val enrollment = caClient.enroll(enrollmentID, enrollmentSecret, enrollmentRequestTLS)
    Logger.info("Successfully performed and retrieved enrollment.")
    val cert = enrollment.getCert
    Logger.info("Retrieved SignedCertificate.")

    // store cert on new chaincode
    val certificateConnection = new ConnectionCertificate(adminName, channel, chaincode, adminWalletPath, networkDescriptionPath)
    certificateConnection.addCertificate(enrollmentID, cert)
    Logger.info("Successfully stored cert on new chaincode")

    cert
  }

  /** Enrolls a new User and stores the X509Identity (KeyPair, SignedCert, MetaData) in the Wallet.
    *
    * @param caURL            Address to find the CA.
    * @param caCert           Certificate to check the validity of the CA.
    * @param walletPath       Wallet to store the new user's identity.
    * @param enrollmentID     enrollmentID of the user to be enrolled.
    * @param enrollmentSecret Password of the user to be enrolled.
    * @param organisationId   Organisation ID, that the user belongs to (MetaInfo for Identity).
    * @throws Exception       if
    *                         1. The CA Client could not be retrieved from the caURL and Certificate.
    *                         2. The enrollment process fails. Maybe your user is not registered?
    */
  def enroll(
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

      val certificateConnection = new ConnectionCertificate(enrollmentID, channel, chaincode, walletPath, networkDescriptionPath)
      certificateConnection.addCertificate(enrollmentID, enrollment.getCert)
      Logger.info("Successfully stored cert on new chaincode")
    }
  }

  /** Creates the enrollmentRequest.
    * If a csr_pem is provided, a garbage KeyPair is generated to fool the faulty hyperledger CSR implementation.
    *
    * @param hostName the hostname associated with the identity.
    * @param profile  the security profile (e.g. "tls").
    * @param csr_pem  (optional) certificate signing request.
    *                 If none is provided, a new KeyPair is generated and used to create a new CSR.
    * @return         the enrollmentRequest object.
    */
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