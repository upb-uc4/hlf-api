package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path
import java.security.cert.X509Certificate

import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, PublicExceptionHelper }
import org.hyperledger.fabric.gateway.{ Identity, Wallet, Wallets, X509Identity }

object WalletManager {
  // get Wallet
  protected[hyperledger] def getWallet(walletPath: Path): Wallet = Wallets.newFileSystemWallet(walletPath)

  /** Gets the X509Certificate from an Identity in the wallet
    *
    * @param walletPath Path to wallet in your filesystem.
    * @param id Identity Name to be accessed in the wallet.
    * @return The X509Certificate-object.
    */
  def getCertificate(walletPath: Path, id: String): X509Certificate = {
    PublicExceptionHelper.wrapInvocationWithNetworkException[X509Certificate](
      () => WalletManager.getX509Identity(walletPath, id).getCertificate
    )
  }

  // get Identity
  protected[hyperledger] def getX509Identity(walletPath: Path, id: String): X509Identity =
    WalletManager.getIdentity(WalletManager.getWallet(walletPath), id).asInstanceOf[X509Identity]
  protected[hyperledger] def getX509Identity(wallet: Wallet, id: String): X509Identity =
    WalletManager.getIdentity(wallet, id).asInstanceOf[X509Identity]
  protected[hyperledger] def getIdentity(walletPath: Path, id: String): Identity =
    WalletManager.getIdentity(getWallet(walletPath), id)
  protected[hyperledger] def getIdentity(wallet: Wallet, id: String): Identity = {
    if (!WalletManager.containsIdentity(wallet, id)) {
      throw Logger.err(s"'$id' credentials not found in wallet: '${wallet.toString}'.")
    }
    wallet.get(id)
  }

  // put Identity
  protected[hyperledger] def putIdentity(walletPath: Path, id: String, identity: Identity): Unit = putIdentity(getWallet(walletPath), id, identity)
  protected[hyperledger] def putIdentity(wallet: Wallet, id: String, identity: Identity): Unit = {
    if (WalletManager.containsIdentity(wallet, id)) {
      throw Logger.err(s"'$id' credentials already present in wallet: '${wallet.toString}'. Cannot put two copies.")
    }
    wallet.put(id, identity)
  }

  /** Checks if a wallet contains a certain identity
    *
    * @param walletPath Path to the wallet in question
    * @param id identity to find
    * @return true if identity is contained, else false
    */
  def containsIdentity(walletPath: Path, id: String): Boolean =
    PublicExceptionHelper.wrapInvocationWithNetworkException[Boolean](
      () => WalletManager.containsIdentity(WalletManager.getWallet(walletPath), id)
    )

  protected[hyperledger] def containsIdentity(wallet: Wallet, id: String): Boolean = wallet.list().contains(id)
}
