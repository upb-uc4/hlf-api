package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait

case class HyperledgerUnhandledException(transactionId: String, innerException: Exception) extends HyperledgerExceptionTrait {
  override def toString(): String =
    "The provided transaction: \"" + transactionId + "\" failed with an unhandled exception:\n" + innerException
}
