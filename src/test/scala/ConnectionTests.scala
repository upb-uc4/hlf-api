import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionCourses
import de.upb.cs.uc4.hyperledger.testBase.TestBaseDevNetwork
import de.upb.cs.uc4.hyperledger.utilities.{GatewayManager, WalletManager}

class ConnectionTests extends TestBaseDevNetwork {

  val contract_name_course: String = "UC4.course"

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
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, network_description_path, id)
        builder should not be null

        // get gateway object
        val gateway = GatewayManager.createGateway(wallet, network_description_path, id)
        gateway should not be null

        // cleanup
        GatewayManager.disposeGateway(gateway)
      }
      "provide a gateway pointing to our network channel" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, network_description_path, id)
        builder should not be null

        // get gateway object
        val gateway = GatewayManager.createGateway(wallet, network_description_path, id)
        gateway should not be null

        try {
          val network = gateway.getNetwork(channel)
          network should not be null
        } finally {
          // cleanup
          GatewayManager.disposeGateway(gateway)
        }
      }
      "provide a gateway pointing to our network channel containing our course_contract" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, network_description_path, id)
        builder should not be null

        // get gateway object
        val gateway = GatewayManager.createGateway(wallet, network_description_path, id)
        gateway should not be null

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
    "asked for a connection" should {
      "provide network connection" in {
        val connection = ConnectionCourses.initialize(id, channel, chaincode, contract_name_course, network_description_path, wallet_path)
        connection should not be null
      }
    }
  }

}
