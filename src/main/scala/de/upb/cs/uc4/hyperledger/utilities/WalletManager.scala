package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway.{Identity, Wallet, Wallets, X509Identity}

object WalletManager {
  def getWallet(walletPath: Path): Wallet = Wallets.newFileSystemWallet(walletPath)
  def getX509Identity(wallet: Wallet, id: String) : X509Identity = getIdentity(wallet, id).asInstanceOf[X509Identity]
  def getIdentity(walletPath: Path, id: String) : Identity = getIdentity(getWallet(walletPath), id)
  def getIdentity(wallet: Wallet, id: String) : Identity = {
    if(!wallet.list().contains(id)){
      throw new Exception(s"'${id}' credentials not found in wallet: '${wallet.toString}'.")
    }
    wallet.get(id)
  }
}
