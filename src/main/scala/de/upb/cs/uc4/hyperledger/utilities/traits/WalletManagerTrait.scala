package de.upb.cs.uc4.hyperledger.utilities.traits

import java.nio.file.Path
import java.security.cert.X509Certificate

import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, PublicExceptionHelper }
import org.hyperledger.fabric.gateway.{ Identity, Wallet, Wallets, X509Identity }

trait WalletManagerTrait {
  /** Gets the X509Certificate from an Identity in the wallet
    *
    * @param walletPath Path to wallet in your filesystem.
    * @param id Identity Name to be accessed in the wallet.
    * @return The X509Certificate-object.
    */
  def getCertificate(walletPath: Path, id: String): X509Certificate

  /** Checks if a wallet contains a certain identity
    *
    * @param walletPath Path to the wallet in question
    * @param id identity to find
    * @return true if identity is contained, else false
    */
  def containsIdentity(walletPath: Path, id: String): Boolean
}
