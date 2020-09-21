package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway.{ Identity, Wallet, Wallets, X509Identity }

protected[hyperledger] object WalletManager {
  // get Wallet
  def getWallet(walletPath: Path): Wallet = Wallets.newFileSystemWallet(walletPath)

  // get Identity
  def getX509Identity(walletPath: Path, id: String): X509Identity =
    WalletManager.getIdentity(WalletManager.getWallet(walletPath), id).asInstanceOf[X509Identity]
  def getX509Identity(wallet: Wallet, id: String): X509Identity = WalletManager.getIdentity(wallet, id).asInstanceOf[X509Identity]
  def getIdentity(walletPath: Path, id: String): Identity = WalletManager.getIdentity(getWallet(walletPath), id)
  def getIdentity(wallet: Wallet, id: String): Identity = {
    if (!WalletManager.containsIdentity(wallet, id)) {
      throw new Exception(s"'${id}' credentials not found in wallet: '${wallet.toString}'.")
    }
    wallet.get(id)
  }

  // put Identity
  def putIdentity(walletPath: Path, id: String, identity: Identity): Unit = putIdentity(getWallet(walletPath), id, identity)
  def putIdentity(wallet: Wallet, id: String, identity: Identity): Unit = {
    if (WalletManager.containsIdentity(wallet, id)) {
      throw new Exception(s"'${id}' credentials already present in wallet: '${wallet.toString}'. Cannot put two copies.")
    }
    wallet.put(id, identity)
  }

  // containsIdentity
  def containsIdentity(walletPath: Path, id: String): Boolean = containsIdentity(getWallet(walletPath), id)
  def containsIdentity(wallet: Wallet, id: String): Boolean = wallet.list().contains(id)
}
