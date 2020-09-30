package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.NetworkExceptionTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

protected[hyperledger] case class NetworkException(
    channel: String,
    chaincode: String,
    networkDescription: String,
    identity: String,
    organisationId: String,
    organisationName: String,
    innerException: Exception
) extends NetworkExceptionTrait {
  override def toString: String = {
    s"""An Exception occured when trying to access the network.
        Your tried to build a connection with these parameters:
          channel   :: $channel
          chaincode :: $chaincode
          networkDescripion :: $networkDescription
          identity :: $identity
          organisationId :: $organisationId
          organisationName :: $organisationName
          ${Logger.getInfoFromException(innerException)}
      """
  }
}

