package de.upb.cs.uc4.hyperledger.exceptions

case class TransactionException(transactionId: String, error: String) extends Exception {
  override def toString: String = s"The provided transaction: '${transactionId}' failed with an error: ${error}"
}
