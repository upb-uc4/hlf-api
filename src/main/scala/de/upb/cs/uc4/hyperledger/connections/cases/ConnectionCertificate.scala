package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

case class ConnectionCertificate(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path) extends ConnectionCertificateTrait {
  final override val contractName: String = "UC4.Certificate"
  override val (contract, gateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.contractName, walletPath, networkDescriptionPath)

  override def addCertificate(enrollmentID: String, certificate: String): String =
    wrapTransactionResult(
      "addCertificate",
      this.internalSubmitTransaction(false, "addCertificate", enrollmentID, certificate)
    )

  override def updateCertificate(enrollmentID: String, certificate: String): String =
    wrapTransactionResult(
      "updateCertificate",
      this.internalSubmitTransaction(false, "updateCertificate", enrollmentID, certificate)
    )

  override def getCertificate(enrollmentId: String): String =
    wrapTransactionResult(
      "getCertificate",
      this.internalEvaluateTransaction("getCertificate", enrollmentId)
    )
}
