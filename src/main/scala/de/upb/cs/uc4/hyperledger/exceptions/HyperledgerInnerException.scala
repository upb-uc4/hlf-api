package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait

case class HyperledgerInnerException(transactionId: String, innerException: Exception) extends HyperledgerExceptionTrait {
  override def toString: String =
    s"The provided transaction: '$transactionId' failed with internal Hyperledger exception:\n$innerException"
}
