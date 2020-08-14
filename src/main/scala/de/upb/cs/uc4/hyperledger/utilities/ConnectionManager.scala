package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway._

object ConnectionManager {

  @throws[Exception]
  def initializeConnection(username: String, channel: String = "myc", chaincode: String = "mycc",
                           contractName: String, wallet_path: Path, network_description_path: Path): (Contract, Gateway) = { // Load a file system based wallet for managing identities.
    println("Try to get connection with: " + network_description_path + "    and: " + wallet_path)

    // get gateway
    val gateway: Gateway = GatewayManager.createGateway(wallet_path, network_description_path, username)

    // get contract
    var contract: Contract = null
    try {
      val network: Network = gateway.getNetwork(channel)
      contract = network.getContract(chaincode, contractName)
    } catch {
      case e: GatewayRuntimeException => GatewayManager.disposeGateway(gateway); throw e;
    }

    // return (contract, gateway) - pair
    (contract, gateway)
  }
}
