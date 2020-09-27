package de.upb.cs.uc4.hyperledger.exceptions.traits

/** Exception Trait to wrap any Exception thrown from our chaincode.
  *  Any validation, misuse of transaction, or similar errors.
  */
trait TransactionExceptionTrait extends Exception {

  /**  Transaction Id provoking the exception. */
  val transactionId: String

  /**  Json-String containing the error-information defined in the API at https://github.com/upb-uc4/api */
  val payload: String

  override def getMessage: String = toString
}
