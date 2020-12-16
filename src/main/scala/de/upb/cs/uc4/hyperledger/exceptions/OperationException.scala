package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ OperationExceptionTrait, TransactionExceptionTrait }

/** Exception Trait to wrap an Exception thrown during submitTransaction */
protected[hyperledger] case class OperationException(approvalResult: String, chainError: TransactionExceptionTrait)
  extends OperationExceptionTrait {
  override def toString: String =
    s"The approval worked: $approvalResult, but the submitTransaction failed: '$chainError'"
}