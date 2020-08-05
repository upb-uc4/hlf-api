package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway.Gateway.Builder
import org.hyperledger.fabric.gateway.{Gateway, Wallet}

object GatewayManager {

  def createGateway(wallet : Wallet, network_config_path : Path, username : String): Gateway = {
    // prepare Network Builder
    val builder: Builder = this.getBuilder(wallet, network_config_path, username)
    builder.connect()
  }

  def getBuilder(wallet: Wallet, networkConfigPath: Path, name: String): Builder = {
    // load a CCP
    val builder = Gateway.createBuilder
    builder.identity(wallet, name).networkConfig(networkConfigPath).discovery(true)

    builder
  }

  def disposeGateway(gateway: Gateway): Unit = {
    if (gateway != null) gateway.close()
  }
}
