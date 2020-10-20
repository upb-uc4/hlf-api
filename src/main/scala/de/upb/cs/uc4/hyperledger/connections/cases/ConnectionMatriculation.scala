package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

case class ConnectionMatriculation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path) extends ConnectionMatriculationTrait {
  final override val contractName: String = "UC4.MatriculationData"
  override val (contract, gateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.contractName, walletPath, networkDescriptionPath)

  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "addMatriculationData", jSonMatriculationData)

  override def addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): String =
    wrapSubmitTransaction(false,       "addEntriesToMatriculationData",enrollmentId, subjectMatriculationList)

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "updateMatriculationData",jSonMatriculationData)

  override def getMatriculationData(enrollmentId: String): String =
    wrapEvaluateTransaction("getMatriculationData",enrollmentId)
}
