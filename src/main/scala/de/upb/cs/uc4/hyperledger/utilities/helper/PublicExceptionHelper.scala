package de.upb.cs.uc4.hyperledger.utilities.helper

import de.upb.cs.uc4.hyperledger.exceptions.NetworkException

object PublicExceptionHelper {

  /** Wraps everything and throws a dedicated NetworkException.
    *
    * @param invocation the method/function/action to be executed
    * @param channel information about the network, provide if possible
    * @param chaincode information about the network, provide if possible
    * @param networkDescription information about the network, provide if possible
    * @param identity information about the network, provide if possible
    * @param organisationId information about the network, provide if possible
    * @param organisationName information about the network, provide if possible
    * @return If successful, this will return whatever was returned by the method/function/action
    */
  def wrapInvocationWithNetworkException[Type](
      invocation: () => Type,
      channel: String = null,
      chaincode: String = null,
      networkDescription: String = null,
      identity: String = null,
      organisationId: String = null
  ): Type = {
    try {
      invocation.apply()
    }
    catch {
      case exception: Exception =>
        throw NetworkException(channel, chaincode, networkDescription,
          identity, organisationId, exception)
    }
  }
}
