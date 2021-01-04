package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

protected[hyperledger] trait ConnectionOperationsTrait extends ConnectionTrait {
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

  /** Submits the "approveTransaction" query.
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
    * @param enrollmentId Information about the enrollmentId.
    * @param state        Information about the state of the operation.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getOperations(enrollmentId: String, state: String): String

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
