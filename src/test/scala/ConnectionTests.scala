import java.nio.file.{Path, Paths}

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConnectionTests extends AnyWordSpec with Matchers {

  val connection_profile_path: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val wallet_path: Path = Paths.get(getClass.getResource("/wallet/").toURI)

  /*
  val connectionManager = ConnectionManager(
    Paths.get(getClass.getResource("/connection_profile.yaml").toURI),
    Paths.get(getClass.getResource("/wallet/").toURI))

  "The connectionManager" when {
    "connecting to Chain" should {

      "provide gateway connection" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)
        wallet should not be null

        // prepare Network Builder
        val builder = connectionManager.getBuilder(wallet)
        builder should not be null

        // get gateway object
        val gateway = builder.connect
        gateway should not be null

        // cleanup
        connectionManager.disposeGateway(gateway)
      }

      "provide network connection" in {
        // retrieve possible identities
        val wallet = connectionManager.getWallet()
        wallet should not be null

        // prepare Network Builder
        val builder = connectionManager.getBuilder(wallet)
        builder should not be null

        // get gateway object
        val gateway = builder.connect
        gateway should not be null

        // try connecting to the network
        try {
          val network = gateway.getNetwork(connectionManager.channel_name)
          network should not be null
        } finally {
          connectionManager.disposeGateway(gateway)
        }
      }

      "Provice chaincode connection" in {
        // test full chaincode connection
        val (gateway, contract_course, contract_student) = connectionManager.initializeConnection()
        gateway should not be null
        contract_course should not be null
        contract_student should not be null

        // cleanup
        connectionManager.disposeGateway(gateway)
      }
    }
  }
   */
}
