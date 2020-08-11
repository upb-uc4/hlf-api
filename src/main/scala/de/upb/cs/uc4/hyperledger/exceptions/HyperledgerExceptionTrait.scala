package de.upb.cs.uc4.hyperledger.exceptions

trait HyperledgerExceptionTrait extends Exception {
  def transactionId: String
  def innerException: Exception
}
