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
      this.internalSubmitTransaction(false, "addMatriculationData", jSonMatriculationData)
    )

  override def addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): String =
    wrapTransactionResult(
      "addEntriesToMatriculationData",
      this.internalSubmitTransaction(false, "addEntriesToMatriculationData", enrollmentId, subjectMatriculationList)
    )

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult(
      "updateMatriculationData",
      this.internalSubmitTransaction(false, "updateMatriculationData", jSonMatriculationData)
    )

  override def getMatriculationData(enrollmentId: String): String =
    wrapTransactionResult(
      "getMatriculationData",
      this.internalEvaluateTransaction("getMatriculationData", enrollmentId)
    )
}
