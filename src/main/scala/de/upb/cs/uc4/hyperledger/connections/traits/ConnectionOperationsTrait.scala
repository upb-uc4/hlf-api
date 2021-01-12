package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionOperationsTrait extends ConnectionTrait {
  final override val contractName: String = "UC4.Approval"

  /** Submits the "approveTransaction" query.
    *
    * @param initiator    Information about the initiator.
    * @param contractName    Information about the transaction.
    * @param transactionName Information about the transaction.
    * @param params          Information about the transaction.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def approveTransaction(initiator: String, contractName: String, transactionName: String, params: String*): String

  /** Submits the "rejectTransaction" query.
    *
    * @param operationId   Information about the operation.
    * @param rejectMessage Information about the reason to reject.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def rejectTransaction(operationId: String, rejectMessage: String): String

  /** Submits the "getOperations" query.
    *
    * @param existingEnrollmentId Filter for the existingApprovals List - leave empty to ignore filter
    * @param missingEnrollmentId Filter for the missingApprovals List - leave empty to ignore filter
    * @param initiatorEnrollmentId Filter for the initiator - leave empty to ignore filter
    * @param state Filter for the operation state - leave empty to ignore filter
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getOperations(existingEnrollmentId: String, missingEnrollmentId: String, initiatorEnrollmentId: String, state: String): String

  /** Submits the "getOperation" query.
    *
    * @param operationId Information about the operation.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getOperationData(operationId: String): String
}
