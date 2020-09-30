package de.upb.cs.uc4.hyperledger.utilities.traits

import java.nio.file.Path
import java.security.{ KeyPair, KeyPairGenerator }

import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionCertificate
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, PublicExceptionHelper }
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest

trait EnrollmentManagerTrait {

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
  ): String

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
  ): Unit
}