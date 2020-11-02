package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionApprovalsTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

case class ConnectionMatriculation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path) extends ConnectionMatriculationTrait {
  final override val contractName: String = "UC4.MatriculationData"
  override val (contract, gateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.contractName, walletPath, networkDescriptionPath)
  override val approvalConnection: Option[ConnectionApprovalsTrait] = Some(ConnectionApproval(username, channel, chaincode, walletPath, networkDescriptionPath))

  def getProposalAddMatriculationData(jSonMatriculationData: String): (Array[Byte], String) = {
    // send as admin maintaining the connection
    addMatriculationData(jSonMatriculationData)
    // TODO: add error handling
    internalGetUnsignedProposal("addMatriculationData", jSonMatriculationData)
  }

  def getProposalAddEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): (Array[Byte], String) = {
    // send as admin maintaining the connection
    addEntriesToMatriculationData(enrollmentId, subjectMatriculationList)
    // TODO: add error handling
    internalGetUnsignedProposal("addEntriesToMatriculationData", enrollmentId: String, subjectMatriculationList: String)
  }

  def getProposalUpdateMatriculationData(jSonMatriculationData: String): (Array[Byte], String) = {
    // send as admin maintaining the connection
    updateMatriculationData(jSonMatriculationData)
    // TODO: add error handling
    internalGetUnsignedProposal("updateMatriculationData", jSonMatriculationData)
  }

  def getProposalGetMatriculationData(enrollmentId: String): (Array[Byte], String) = {
    // send as admin maintaining the connection
    getMatriculationData(enrollmentId)
    // TODO: add error handling
    internalGetUnsignedProposal("getMatriculationData", enrollmentId)
  }

  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "addMatriculationData", jSonMatriculationData)

  override def addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): String =
    wrapSubmitTransaction(false, "addEntriesToMatriculationData", enrollmentId, subjectMatriculationList)

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "updateMatriculationData", jSonMatriculationData)

  override def getMatriculationData(enrollmentId: String): String =
    wrapEvaluateTransaction("getMatriculationData", enrollmentId)
}
