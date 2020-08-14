package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.{HyperledgerExceptionTrait, TransactionExceptionTrait}
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

case class ConnectionMatriculation(id: String, channel: String, chaincode: String, wallet_path: Path, network_description_path: Path) extends ConnectionMatriculationTrait {
  val contract_name: String = "UC4.MatriculationData"
  override val (contract, gateway) = ConnectionManager.initializeConnection(id, channel, chaincode, this.contract_name, network_description_path, wallet_path)

  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult("addMatriculationData",
      this.internalSubmitTransaction("addMatriculationData", jSonMatriculationData))

  override def addEntryToMatriculationData(matriculationId: String, fieldOfStudy: String, semester: String): String =
    wrapTransactionResult("addEntryToMatriculationData",
      this.internalSubmitTransaction("addEntryToMatriculationData", matriculationId, fieldOfStudy, semester))

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult("updateMatriculationData",
      this.internalSubmitTransaction("updateMatriculationData", jSonMatriculationData))

  override def getMatriculationData(matId: String): String =
    wrapTransactionResult("getMatriculationData",
      this.internalEvaluateTransaction("getMatriculationData", matId))
}