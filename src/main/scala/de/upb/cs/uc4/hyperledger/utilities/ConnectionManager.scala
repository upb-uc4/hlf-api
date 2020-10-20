package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, PublicExceptionHelper }
import org.hyperledger.fabric.gateway._
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl, NetworkImpl }

/** Manager for all things ConnectionRelated.
  *  Can be used to retrieve contract and gateway from
  */
protected[hyperledger] object ConnectionManager {

  /** Retrieves a gateway to communicate with the hyperledger network
    * Retrieves a Contract to invoke transactions on.
    * @param username name of the certificate to use when communicating
    * @param channel name of the channel / network
    * @param chaincode name of the chaincode to access
    * @param contractName name of the contract / domain of the contract
    * @param walletPath path to the certificate wallet
    * @param networkDescriptionPath path to a configuration-file describing the network (IP addresses of peers)
    * @throws GatewayRuntimeException when gateway cannot find channel / network
    * @return contract and gateway objects
    */
  @throws[GatewayRuntimeException]
  def initializeConnection(
      username: String,
      channel: String = "myc",
      chaincode: String = "mycc",
      contractName: String,
      walletPath: Path,
      networkDescriptionPath: Path
  ): (ContractImpl, GatewayImpl) = {
    PublicExceptionHelper.wrapInvocationWithNetworkException[(ContractImpl, GatewayImpl)](
      () => {
        Logger.info(s"Try to get connection with: '$networkDescriptionPath' and: '$walletPath'")
        // get gateway
        val gateway: GatewayImpl = GatewayManager.createGateway(walletPath, networkDescriptionPath, username)

        var contract: ContractImpl = null
        try {
          contract = ConnectionManager.retrieveContract(gateway, channel, chaincode, contractName)
        }
        catch {
          case e: GatewayRuntimeException => {
            GatewayManager.disposeGateway(gateway)
            throw Logger.err(s"Could not retrieve contract $contractName from chaincode $chaincode in channel $channel.", e)
          }
        }

        // return (contract, gateway) - pair
        (contract, gateway)
      },
      channel, chaincode, networkDescriptionPath.toString, username
    )
  }

  private def checkConnectionInitialized(network: Network): Unit =
    if (!network.getChannel.isInitialized) throw new Exception("Network could not be initialized.")

  /** Creates a Contract to invoke transactions on.
    * @param gateway Gateway to the network to connect with
    * @param channelName name of the channel / network
    * @param chaincodeName name of the chaincode to access
    * @param contractName name of the contract / domain of the contract
    * @throws GatewayRuntimeException when gateway cannot find channel / network
    * @return contract object to invoke transactions on
    */
  @throws[GatewayRuntimeException]
  private def retrieveContract(
      gateway: GatewayImpl,
      channelName: String,
      chaincodeName: String,
      contractName: String
  ): ContractImpl = {
    // get network (channel)
    val network: NetworkImpl = gateway.getNetwork(channelName).asInstanceOf[NetworkImpl]
    checkConnectionInitialized(network)

    // get contract (chaincode, contract)
    network.getContract(chaincodeName, contractName).asInstanceOf[ContractImpl]
  }
}
