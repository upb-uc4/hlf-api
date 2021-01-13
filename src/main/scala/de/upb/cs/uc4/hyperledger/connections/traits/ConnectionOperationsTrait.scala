package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionOperationsTrait extends ConnectionTrait {
  final override val contractName: String = "UC4.OperationData"

  /** Submits the "proposeTransaction" query.
    *
    * @param initiator       Information about the initiator.
    * @param contractName    Information about the transaction.
    * @param transactionName Information about the transaction.
    * @param params          Information about the transaction.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return OperationData after the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def proposeTransaction(initiator: String, contractName: String, transactionName: String, params: String*): String

  /** Retrieves a proposal for the designated query
    *
    * @param initiator       Information about the initiator.
    * @param contractName    Information about the transaction.
    * @param transactionName Information about the transaction.
    * @param params          Information about the transaction.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return OperationData after the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalProposeTransaction(certificate: String, affiliation: String = AFFILIATION, initiator: String, contractName: String, transactionName: String, params: Array[String]): Array[Byte]

  /** Retrieves a proposal for the designated query
    *
    * @param operationId Identifier for the operation to approve.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return OperationData after the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalApproveOperation(certificate: String, affiliation: String = AFFILIATION, operationId: String): Array[Byte]

  /** Retrieves a proposal for the designated query
    *
    * @param operationId   Identifier for the operation to reject.
    * @param rejectMessage The given reason to reject.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return OperationData after the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalRejectOperation(certificate: String, affiliation: String = AFFILIATION, operationId: String, rejectMessage: String): Array[Byte]

  /** Submits the "approveOperation" query.
    *
    * @param operationId Identifier for the operation to approve.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return OperationData after the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def approveOperation(operationId: String): String

  /** Submits the "rejectOperation" query.
    *
    * @param operationId   Identifier for the operation to reject.
    * @param rejectMessage The given reason to reject.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return OperationData after the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def rejectOperation(operationId: String, rejectMessage: String): String

  /** Submits the "getOperations" query.
    *
    * @param operationIds          Filter for the operationIds - leave empty to ignore filter
    * @param existingEnrollmentId  Filter for the existingApprovals List - leave empty to ignore filter
    * @param missingEnrollmentId   Filter for the missingApprovals List - leave empty to ignore filter
    * @param initiatorEnrollmentId Filter for the initiator - leave empty to ignore filter
    * @param involvedEnrollmentId  Logical OR filter for the existingEnrollmentId, missingEnrollmentId and initiatorEnrollmentId - leave empty to ignore filter
    * @param states                Filter for the operation state - leave empty to ignore filter
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getOperations(operationIds: List[String], existingEnrollmentId: String, missingEnrollmentId: String, initiatorEnrollmentId: String, involvedEnrollmentId: String, states: List[String]): String
}
