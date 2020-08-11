package de.upb.cs.uc4.hyperledger.exceptions.traits

trait TransactionExceptionTrait extends Exception {
  def transactionId: String

  def jsonError: String
}
