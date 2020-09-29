package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionCertificateTrait extends ConnectionTrait {

  /** Executes the "addCertificate" query.
    *
    * @param enrollmentID Information about the enrollmentID to add.
    * @param certificate Information about the certificate to add.
    * @throws Exception if chaincode throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addCertificate(enrollmentID: String, certificate: String): String

  /** Submits the "updateCertificate" query.
    *
    * @param enrollmentID enrollmentID to update
    * @param certificate certificate to update
    * @throws Exception if chaincode throws an exception.
    * @return success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def updateCertificate(enrollmentID: String, certificate: String): String

  /** Executes the "getCertificate" query.
    *
    * @param enrollmentId enrollment.id to get information
    * @throws Exception if chaincode throws an exception.
    * @return JSon Course Object
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getCertificate(enrollmentId: String): String
}
