package de.upb.cs.uc4.hyperledger.exceptions

case class TransactionException(transactionId: String, jsonError: String) extends TransactionExceptionTrait{
  override def toString: String =
    s"The provided transaction: '${transactionId}' failed with an error: ${jsonError}"
}