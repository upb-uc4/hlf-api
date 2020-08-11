package de.upb.cs.uc4.hyperledger.utilities

import java.nio.file.Path

import org.hyperledger.fabric.gateway.{Wallet, Wallets}

object WalletManager {
  def getWallet(wallet_path : Path): Wallet = {
    Wallets.newFileSystemWallet(wallet_path)
  }
}
