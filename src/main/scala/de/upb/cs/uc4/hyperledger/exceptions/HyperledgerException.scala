package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

case class HyperledgerException(
    transactionName: String,
    innerException: Exception
) extends HyperledgerExceptionTrait {
  override def toString: String =
    s"""
        The provided transaction: '$transactionName' failed with internal Hyperledger exception:
        ${Logger.getInfoFromException(innerException)}
    """
}
