package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.{ ConnectionManager, GatewayManager, WalletManager }

class ManagerTests extends TestBase {

  "The WalletManager" when {
    "asked for a wallet" should {
      "not return null" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(walletPath)
        wallet should not be null
      }
      "contain expected id" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(walletPath)
        val contained = wallet.list.contains(username)
        contained should equal(true)
      }
    }
  }

  "The GatewayManager" when {
    "asked to setup a gateway" should {
      "provide a builder" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(walletPath)

        // prepare Network Builder
        val builder = GatewayManager.getBuilder(wallet, networkDescriptionPath, username)
        builder should not be null
      }

      "provide a gateway" in {
        // get gateway object
        val gateway = GatewayManager.createGateway(walletPath, networkDescriptionPath, username)
        gateway should not be null

        // cleanup
        GatewayManager.disposeGateway(gateway)
      }

      "provide a gateway pointing to our network channel" in {
        // get gateway object
        val gateway = GatewayManager.createGateway(walletPath, networkDescriptionPath, username)

        try {
          val network = gateway.getNetwork(channel)
          network should not be null
        }
        finally {
          // cleanup
          GatewayManager.disposeGateway(gateway)
        }
      }

      "provide a gateway pointing to our network channel containing our course_contract" in {
        // get gateway object
        val gateway = GatewayManager.createGateway(walletPath, networkDescriptionPath, username)

        try {
          val network = gateway.getNetwork(channel)
          val contract = network.getContract(chaincode, contractNameCourse)
          contract should not be null
        }
        finally {
          // cleanup
          GatewayManager.disposeGateway(gateway)
        }
      }
    }
  }

  "The Connection Manager" when {
    "asked for connection info" should {
      "provide network connection info - courses" in {
        val (contract, gateway) = ConnectionManager.initializeConnection(
          username, channel, chaincode, contractNameCourse, walletPath, networkDescriptionPath
        )
        contract should not be null
        gateway should not be null
        GatewayManager.disposeGateway(gateway)
      }

      "provide network connection info - matriculation" in {
        val (contract, gateway) = ConnectionManager.initializeConnection(
          username, channel, chaincode, contractNameMatriculation, walletPath, networkDescriptionPath
        )
        contract should not be null
        gateway should not be null
        GatewayManager.disposeGateway(gateway)
      }
    }
  }

}
