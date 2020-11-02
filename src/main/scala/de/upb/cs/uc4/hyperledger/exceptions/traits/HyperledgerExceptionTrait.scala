package de.upb.cs.uc4.hyperledger.exceptions.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.internal.UC4ExceptionTrait

/** Exception Trait to wrap any Exception thrown during a transaction from the Hyperledger Framework */
trait HyperledgerExceptionTrait extends UC4ExceptionTrait {

  /**  Action Name provoking the exception. */
  def actionName: String

  /**  Inner Exception thrown from the Hyperledger Framework */
  def innerException: Throwable
}
