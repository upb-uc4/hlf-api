package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

case class ConnectionMatriculation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path) extends ConnectionMatriculationTrait {
  final override val contractName: String = "UC4.MatriculationData"
  override val (contract, gateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.contractName, walletPath, networkDescriptionPath)

  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult(
      "addMatriculationData",
      this.internalSubmitTransaction(true, "addMatriculationData", jSonMatriculationData)
    )

  override def addEntriesToMatriculationData(matriculationId: String, subjectMatriculationList: String): String =
    wrapTransactionResult(
      "addEntryToMatriculationData",
      this.internalSubmitTransaction(false, "addEntriesToMatriculationData", matriculationId, subjectMatriculationList)
    )

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult(
      "updateMatriculationData",
      this.internalSubmitTransaction(true, "updateMatriculationData", jSonMatriculationData)
    )

  override def getMatriculationData(matId: String): String =
    wrapTransactionResult(
      "getMatriculationData",
      this.internalEvaluateTransaction("getMatriculationData", matId)
    )
}