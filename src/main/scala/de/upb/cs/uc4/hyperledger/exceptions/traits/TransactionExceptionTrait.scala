package de.upb.cs.uc4.hyperledger.exceptions.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.internal.UC4ExceptionTrait

/** Exception Trait to wrap any Exception thrown from our chaincode-contracts.
  * Any validation, misuse of transaction, or similar errors.
  */
trait TransactionExceptionTrait extends UC4ExceptionTrait {

  /**  transaction Name provoking the exception. */
  val transactionName: String

  /**  Json-String containing the error-information defined in the API at https://github.com/upb-uc4/api */
  val payload: String
}
