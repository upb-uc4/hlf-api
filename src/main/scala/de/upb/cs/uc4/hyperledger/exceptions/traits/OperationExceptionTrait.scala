package de.upb.cs.uc4.hyperledger.exceptions.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.internal.UC4ExceptionTrait

/** Exception Trait to wrap any Exception thrown from our chaincode-contracts.
  * Any validation, misuse of transaction, or similar errors.
  */
trait OperationExceptionTrait extends UC4ExceptionTrait {

  /** whatever was returned from the approval-transaction. */
  val approvalResult: String

  /** The Exception that would have been thrown if the transaction would have been invoked regularly */
  val chainError: TransactionExceptionTrait
}
