package de.upb.cs.uc4.hyperledger.exceptions

trait TransactionExceptionTrait extends Exception {
  def transactionId: String
  def jsonError: String
}
