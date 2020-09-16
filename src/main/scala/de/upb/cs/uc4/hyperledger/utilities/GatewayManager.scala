package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway.Gateway.Builder
import org.hyperledger.fabric.gateway.{Gateway, Identity, Wallet}

object GatewayManager {

  def createGateway(walletPath: Path, networkDescriptionPath: Path, username: String): Gateway =
    this.createGateway(WalletManager.getWallet(walletPath), networkDescriptionPath, username)
  def createGateway(wallet: Wallet, networkDescriptionPath: Path, username: String): Gateway =
    this.createGateway(WalletManager.getIdentity(wallet, username), networkDescriptionPath)
  def createGateway(identity: Identity, networkDescriptionPath: Path): Gateway = {
    Gateway.createBuilder()
      .identity(identity)
      .networkConfig(networkDescriptionPath)
      .connect()
  }

  /** Close a connection to a network.
    *
    * @param gateway The connection to close.
    */
  def disposeGateway(gateway: Gateway): Unit = {
    if (gateway != null) gateway.close()
  }
}
