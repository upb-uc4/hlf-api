package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionMatriculationTrait extends ConnectionTrait {

  /** Retrieves a proposal for the designated query
    * Also submits the "addMatriculationData" query as current user (admin).
    *
    * @param jSonMatriculationData Information about the matriculation to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddMatriculationData(certificate: String, affiliation: String, jSonMatriculationData: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the "addEntriesToMatriculationData" query as current user (admin).
    *
    * @param enrollmentId enrollment.id to add entry to
    * @param subjectMatriculationList a Json object containing the List of subjectMatriculationInfo - objects
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddEntriesToMatriculationData(certificate: String, affiliation: String, enrollmentId: String, subjectMatriculationList: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the "updateMatriculationData" query as current user (admin).
    *
    * @param jSonMatriculationData matriculationInfo to update
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalUpdateMatriculationData(certificate: String, affiliation: String, jSonMatriculationData: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the "getMatriculationData" query as current user (admin).
    *
    * @param enrollmentId enrollment.id to get information
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetMatriculationData(certificate: String, affiliation: String, enrollmentId: String): (String, Array[Byte])

  /** Submits the "addMatriculationData" query.
    *
    * @param jSonMatriculationData Information about the matriculation to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addMatriculationData(jSonMatriculationData: String): String

  /** Submits the "addEntryToMatriculationData" query.
    *
    * @param enrollmentId enrollment.id to add entry to
    * @param subjectMatriculationList a Json object containing the List of subjectMatriculationInfo - objects
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): String

  /** Submits the "updateMatriculationData" query.
    *
    * @param jSonMatriculationData matriculationInfo to update
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def updateMatriculationData(jSonMatriculationData: String): String

  /** Executes the "getMatriculationData" query.
    *
    * @param enrollmentId enrollment.id to get information
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return JSon Matriculation Object
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getMatriculationData(enrollmentId: String): String
}
