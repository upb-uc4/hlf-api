package de.upb.cs.uc4.hyperledger.exceptions.traits

/**
 *  Exception Trait to wrap any Exception thrown from the Hyperledger Framework
 */
trait HyperledgerExceptionTrait extends Exception {

  /**
   *  Transaction Id provoking the exception.
   */
  def transactionId: String

  /**
   *  Inenr Exception thrown from the Hyperledger Framework
   */
  def innerException: Exception
}
