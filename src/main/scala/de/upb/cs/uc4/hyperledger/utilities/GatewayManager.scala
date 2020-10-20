package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway.impl.GatewayImpl
import org.hyperledger.fabric.gateway.{ Gateway, Identity }

protected[hyperledger] object GatewayManager {

  def createGateway(walletPath: Path, networkDescriptionPath: Path, username: String): GatewayImpl =
    this.createGateway(WalletManager.getIdentity(walletPath, username), networkDescriptionPath)
  def createGateway(identity: Identity, networkDescriptionPath: Path): GatewayImpl = {
    Gateway.createBuilder()
      .identity(identity)
      .networkConfig(networkDescriptionPath)
      .connect().asInstanceOf[GatewayImpl]
  }

  /** Close a connection to a network.
    *
    * @param gateway The connection to close.
    */
  def disposeGateway(gateway: Gateway): Unit = {
    if (gateway != null) gateway.close()
  }
}
