import de.upb.cs.uc4.hyperledger.testBase.TestBaseDevNetwork
import de.upb.cs.uc4.hyperledger.utilities.{ConnectionManager, GatewayManager, WalletManager}

class ManagerTests extends TestBaseDevNetwork {

  "The WalletManager" when {
    "asked for a wallet" should {
      "not return null" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)
        wallet should not be null
      }
      "contain expected id" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)
        val contained = wallet.list.contains(id)
        contained should equal (true)
      }
    }
  }

  "The GatewayManager" when {
    "asked to setup a gateway" should {
      "provide a builder" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, network_description_path, id)
        builder should not be null
      }

      "provide a gateway" in {
        // get gateway object
        val gateway = GatewayManager.createGateway(wallet_path, network_description_path, id)
        gateway should not be null

        // cleanup
        GatewayManager.disposeGateway(gateway)
      }

      "provide a gateway pointing to our network channel" in {
        // get gateway object
        val gateway = GatewayManager.createGateway(wallet_path, network_description_path, id)

        try {
          val network = gateway.getNetwork(channel)
          network should not be null
        } finally {
          // cleanup
          GatewayManager.disposeGateway(gateway)
        }
      }

      "provide a gateway pointing to our network channel containing our course_contract" in {
        // get gateway object
        val gateway = GatewayManager.createGateway(wallet_path, network_description_path, id)

        val contract_name_course: String = "UC4.course"

        try {
          val network = gateway.getNetwork(channel)
          val contract = network.getContract(chaincode, contract_name_course)
          contract should not be null
        } finally {
          // cleanup
          GatewayManager.disposeGateway(gateway)
        }
      }
    }
  }

  "The Connection Manager" when {
    "asked for connection info" should {
      "provide network connection info - courses" in {

        val contract_name_course: String = "UC4.course"

        val (contract, gateway) = ConnectionManager.initializeConnection(
          id, channel, chaincode, contract_name_course, wallet_path, network_description_path)
        contract should not be null
        gateway should not be null
        GatewayManager.disposeGateway(gateway)
      }

      "provide network connection info - matriculation" in {

        val contract_name_matriculation: String = "UC4.MatriculationData"

        val (contract, gateway) = ConnectionManager.initializeConnection(
          id, channel, chaincode, contract_name_matriculation, wallet_path, network_description_path)
        contract should not be null
        gateway should not be null
        GatewayManager.disposeGateway(gateway)
      }
    }
  }

}
