import java.nio.file.{Path, Paths}

import de.upb.cs.uc4.hyperledger.ConnectionManager
import de.upb.cs.uc4.hyperledger.utilities.{GatewayManager, WalletManager}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConnectionTests extends AnyWordSpec with Matchers {

  val connection_profile_path: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val wallet_path: Path = Paths.get(getClass.getResource("/wallet/").toURI)
  val contained_id: String = "cli"
  val channel_name: String = "myc"
  val chaincode: String = "mycc"
  val contract_name_course = "UC4.course"

  val connectionManager: ConnectionManager = ConnectionManager(connection_profile_path, wallet_path)

  "The WalletManager" when {
    "asked for a wallet" should {
      "not return null" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)
        wallet should not be null
      }
      "contain cli" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)
        wallet.list() should contain this.contained_id
      }
    }
  }

  "The GatewayManager" when {
    "asked to setup a gateway" should {
      "provide a builder" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, connection_profile_path, contained_id)
        builder should not be null
      }

      "provide a gateway" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, connection_profile_path, contained_id)
        builder should not be null

        // get gateway object
        val gateway = GatewayManager.createGateway(wallet, connection_profile_path, contained_id)
        gateway should not be null

        // cleanup
        GatewayManager.disposeGateway(gateway)
      }
      "provide a gateway pointing to our network channel" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, connection_profile_path, contained_id)
        builder should not be null

        // get gateway object
        val gateway = GatewayManager.createGateway(wallet, connection_profile_path, contained_id)
        gateway should not be null

        try {
          val network = gateway.getNetwork(channel_name)
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
        val builder = GatewayManager.getBuilder(wallet, connection_profile_path, contained_id)
        builder should not be null

        // get gateway object
        val gateway = GatewayManager.createGateway(wallet, connection_profile_path, contained_id)
        gateway should not be null

        try {
          val network = gateway.getNetwork(channel_name)
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
        val connection = connectionManager.createConnection()
        connection should not be null
      }
    }
  }
}
