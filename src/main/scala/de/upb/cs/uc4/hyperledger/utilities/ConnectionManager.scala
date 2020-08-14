package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway._

/**
 *  Manager for all things ConnectionRelated.
 *  Can be used to retrieve contract and gateway from
 */
object ConnectionManager {

  /**
   *  Retrieves a gateway to communicate with the hyperledger network
   *  Retrieves a Contract to invoke transactions on.
   * @param username name of the certificate to use when communicating
   * @param channel name of the channel / network
   * @param chaincode name of the chaincode to access
   * @param contractName name of the contract / domain of the contract
   * @param wallet_path path to the certificate wallet
   * @param network_description_path path to a configuration-file describing the network (IP addresses of peers)
   * @throws GatewayRuntimeException when gateway cannot find channel / network
   * @return contract and gateway objects
   */
  @throws[GatewayRuntimeException]
  def initializeConnection(username: String, channel: String = "myc", chaincode: String = "mycc",
                           contractName: String, wallet_path: Path, network_description_path: Path): (Contract, Gateway) = { // Load a file system based wallet for managing identities.
    println("Try to get connection with: " + network_description_path + "    and: " + wallet_path)

    // get gateway
    val gateway: Gateway = GatewayManager.createGateway(wallet_path, network_description_path, username)

    // get contract
    var contract: Contract = null
    try {
      contract = ConnectionManager.retrieveContract(gateway, channel, chaincode, contractName)
    } catch {
      case e: GatewayRuntimeException => GatewayManager.disposeGateway(gateway); throw e;
    }

    // return (contract, gateway) - pair
    (contract, gateway)
  }

  /**
   *  Creates a Contract to invoke transactions on.
   * @param gateway Gateway to the network to conenct with
   * @param channel name of the channel / network
   * @param chaincode name of the chaincode to access
   * @param contractName name of the contract / domain of the contract
   * @throws GatewayRuntimeException when gateway cannot find channel / network
   * @return contract object to invoke transactions on
   */
  @throws[GatewayRuntimeException]
  def retrieveContract(gateway: Gateway,
                           channel: String = "myc",
                           chaincode: String = "mycc",
                           contractName: String): Contract = {
    // get network (channel)
    val network: Network = gateway.getNetwork(channel)
    // get contract (chaincode, contract)
    val contract = network.getContract(chaincode, contractName)

    contract
  }
}
