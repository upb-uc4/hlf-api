package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.NetworkExceptionTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

protected[hyperledger] case class NetworkException(
    channel: String = null,
    chaincode: String = null,
    networkDescription: String = null,
    identity: String = null,
    organisationId: String = null,
    organisationName: String = null,
    innerException: Exception = null
) extends NetworkExceptionTrait {
  override def toString: String = {
    s"""An Exception occurred when trying to access the network.
        Your tried to build a connection with these parameters:
          channel   :: $channel
          chaincode :: $chaincode
          networkDescription :: $networkDescription
          identity :: $identity
          organisationId :: $organisationId
          organisationName :: $organisationName
          ${Logger.getInfoFromException(innerException)}
      """
  }
}

