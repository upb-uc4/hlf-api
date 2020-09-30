package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway._

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
  ): (Contract, Gateway) = {
    Logger.info(s"Try to get connection with: '$networkDescriptionPath' and: '$walletPath'")
    // get gateway
    val gateway: Gateway = GatewayManager.createGateway(walletPath, networkDescriptionPath, username)

    // get contract
    var contract: Contract = null
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
  }



  /** Creates a Contract to invoke transactions on.
    * @param gateway Gateway to the network to conenct with
    * @param channelName name of the channel / network
    * @param chaincodeName name of the chaincode to access
    * @param contractName name of the contract / domain of the contract
    * @throws GatewayRuntimeException when gateway cannot find channel / network
    * @return contract object to invoke transactions on
    */
  @throws[GatewayRuntimeException]
  private def retrieveContract(
      gateway: Gateway,
      channelName: String,
      chaincodeName: String,
      contractName: String
  ): Contract = {
    // get network (channel)
    val network: Network = gateway.getNetwork(channelName)
    // get contract (chaincode, contract)
    val contract = network.getContract(chaincodeName, contractName)

    contract
  }
}
