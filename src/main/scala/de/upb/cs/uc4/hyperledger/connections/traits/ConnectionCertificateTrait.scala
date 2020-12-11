package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionCertificateTrait extends ConnectionTrait {
  final override val contractName: String = "UC4.Certificate"

  /** Retrieves a proposal for the designated query
    * Also submits the "addCertificate" query as current user (admin).
    *
    * @param enrollmentID Information about the enrollmentID to add.
    * @param certificate Information about the certificate to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddCertificate(certificate: String, affiliation: String = AFFILITATION, enrollmentID: String, newCertificate: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the "updateCertificate" query as current user (admin).
    *
    * @param enrollmentID enrollmentID to update
    * @param certificate certificate to update
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalUpdateCertificate(certificate: String, affiliation: String = AFFILITATION, enrollmentID: String, newCertificate: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the "getCertificate" query as current user (admin).
    *
    * @param enrollmentID enrollmentID to update
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetCertificate(certificate: String, affiliation: String = AFFILITATION, enrollmentID: String): (String, Array[Byte])

  /** Submits the "addCertificate" query.
    *
    * @param enrollmentID Information about the enrollmentID to add.
    * @param certificate Information about the certificate to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addCertificate(enrollmentID: String, certificate: String): String

  /** Submits the "updateCertificate" query.
    *
    * @param enrollmentID enrollmentID to update
    * @param certificate certificate to update
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def updateCertificate(enrollmentID: String, certificate: String): String

  /** Executes the "getCertificate" query.
    *
    * @param enrollmentId enrollment.id to get information
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Certificate String
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getCertificate(enrollmentId: String): String

  /** Stores a new certificate on the chain. If no certificate for the user exists, we add it, else update.
    *
    * @param enrollmentID enrollmentID to add or update
    * @param certificate certificate to add or update
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addOrUpdateCertificate(enrollmentID: String, certificate: String): String
}
