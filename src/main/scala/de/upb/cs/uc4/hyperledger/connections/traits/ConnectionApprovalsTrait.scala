package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

protected[connections] trait ConnectionApprovalsTrait extends ConnectionTrait {

  /** Submits the "approveTransaction" query.
    *
    * @param transactionName Information about the transaction.
    * @param params Information about the transaction.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def approveTransaction(transactionName: String, params: String*): String


  /** Submits the "getApprovals" query.
    *
    * @param transactionName Information about the transaction.
    * @param params Information about the transaction.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getApprovals(transactionName: String, params: String*): String
}
