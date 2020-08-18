package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway.Gateway.Builder
import org.hyperledger.fabric.gateway.{ Gateway, Wallet }

object GatewayManager {

  def createGateway(walletPath: Path, networkDescriptionPath: Path, username: String): Gateway = {
    val wallet: Wallet = WalletManager.getWallet(walletPath)

    // prepare Network Builder
    this.createGateway(wallet, networkDescriptionPath, username)
  }

  def createGateway(wallet: Wallet, networkDescriptionPath: Path, username: String): Gateway = {
    // prepare Network Builder
    val builder: Builder = this.getBuilder(wallet, networkDescriptionPath, username)
    builder.connect()
  }

  def getBuilder(wallet: Wallet, networkDescriptionPath: Path, name: String): Builder = {
    // load a CCP
    var builder = Gateway.createBuilder
    builder = builder.identity(wallet, name)
    builder = builder.networkConfig(networkDescriptionPath)

    builder
  }

  def disposeGateway(gateway: Gateway): Unit = {
    if (gateway != null) gateway.close()
  }
}
