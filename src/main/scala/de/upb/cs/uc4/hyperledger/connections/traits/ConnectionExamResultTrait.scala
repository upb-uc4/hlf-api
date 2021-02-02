package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionExamResultTrait extends ConnectionTrait {
  final override val contractName: String = "UC4.ExamResult"

  /** Retrieves a proposal for the designated query
    * Also submits approval for the query as current user (admin).
    *
    * @param examResultJson Information about the exam result to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The Proposal requested.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddExamResult(certificate: String, affiliation: String = AFFILIATION, examResultJson: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits approval for the query as current user (admin).
    *
    * @param enrollmentId        Filter for the enrollmentId - leave empty to ignore filter
    * @param examIds        Filter for the examIds - leave empty to ignore filter
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The Proposal requested.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetExamResultEntries(certificate: String, affiliation: String = AFFILIATION,
      enrollmentId: String, examIds: List[String]): (String, Array[Byte])

  /** Submits the "addExamResult" query.
    *
    * @param examJson Information about the exam to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The final object that is present on the chain after performing the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addExamResult(examJson: String): String

  /** Submits the "getExamResultEntries" query.
    *
    * @param enrollmentId        Filter for the enrollmentId - leave empty to ignore filter
    * @param examIds        Filter for the examIds - leave empty to ignore filter
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The final object that is present on the chain after performing the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getExamResultEntries(enrollmentId: String, examIds: List[String]): String
}
