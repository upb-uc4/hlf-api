package de.upb.cs.uc4.hyperledger

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.traits.{ChaincodeActionsTrait, ConnectionManagerTrait}
import de.upb.cs.uc4.hyperledger.utilities.{GatewayManager, WalletManager}
import org.hyperledger.fabric.gateway._

/**
 * Manager to engage in communication with the HyperledgerNetwork
 *
 * @param connection_profile_path Path to connectionProfile.yaml
 * @param wallet_path             Path to wallet dictionary containing all certificates
 */
case class ConnectionManager(connection_profile_path: Path, wallet_path: Path)
  extends ConnectionManagerTrait {

  private val contract_name_course = "UC4.course"
  private val contract_name_student = "UC4.student"

  override def createConnection(username: String = "cli", channel: String = "myc", chaincode: String = "mycc"): ChaincodeActionsTrait = {
    val (gateway: Gateway, contract_course: Contract, contract_student: Contract) = this.initializeConnection(username, channel, chaincode)
    new ChaincodeConnection(gateway, contract_course, contract_student)
  }

  @throws[Exception]
  def initializeConnection(username: String, channel: String = "myc", chaincode: String = "mycc"): (Gateway, Contract, Contract) = { // Load a file system based wallet for managing identities.
    println("Try to get connection with: " + connection_profile_path + "    and: " + wallet_path)

    // retrieve possible identities
    val wallet: Wallet = WalletManager.getWallet(this.wallet_path)

    val gateway: Gateway = GatewayManager.createGateway(wallet, this.connection_profile_path, username)

    var contract_course: Contract = null
    var contract_student: Contract = null
    try {
      val network: Network = gateway.getNetwork(channel)
      contract_course = network.getContract(chaincode, this.contract_name_course)
      contract_student = network.getContract(chaincode, this.contract_name_student)
    } catch {
      case e: GatewayRuntimeException => GatewayManager.disposeGateway(gateway); throw e;
    }

    return (gateway, contract_course, contract_student)
  }


  System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true")
}
